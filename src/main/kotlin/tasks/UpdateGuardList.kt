package me.hbj.bikkuri.tasks

import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import me.hbj.bikkuri.Bikkuri
import me.hbj.bikkuri.client
import me.hbj.bikkuri.data.General
import me.hbj.bikkuri.data.GuardFetcher
import me.hbj.bikkuri.data.ListenerData
import me.hbj.bikkuri.db.GuardLastUpdate
import me.hbj.bikkuri.db.GuardList
import me.hbj.bikkuri.exception.ReconnectException
import me.hbj.bikkuri.util.after1Day
import me.hbj.bikkuri.util.after30Days
import me.hbj.bikkuri.util.now
import moe.sdl.yabapi.api.createLiveDanmakuConnection
import moe.sdl.yabapi.api.getBasicInfo
import moe.sdl.yabapi.api.getGuardList
import moe.sdl.yabapi.api.getLiveDanmakuInfo
import moe.sdl.yabapi.api.getRoomIdByUid
import moe.sdl.yabapi.api.getRoomInitInfo
import moe.sdl.yabapi.connect.LiveDanmakuConnectConfig
import moe.sdl.yabapi.connect.onCertificateResponse
import moe.sdl.yabapi.connect.onCommandResponse
import moe.sdl.yabapi.connect.onHeartbeatResponse
import moe.sdl.yabapi.data.live.GuardLevel
import moe.sdl.yabapi.data.live.LiveDanmakuInfoGetResponse
import moe.sdl.yabapi.data.live.commands.DanmakuMsgCmd
import moe.sdl.yabapi.data.live.commands.GuardBuyCmd
import moe.sdl.yabapi.data.live.commands.SuperChatMsgCmd
import mu.KotlinLogging
import net.mamoe.mirai.console.util.retryCatching
import java.io.IOException
import java.nio.channels.UnresolvedAddressException
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private val logger = KotlinLogging.logger {}

// mid to job
internal val jobMap: MutableMap<Int, Job> = mutableMapOf()

fun CoroutineScope.launchUpdateGuardListTask(): Job = launch {
  if (!client.getBasicInfo().data.isLogin) {
    Bikkuri.logger.info("检测到未登录，请使用 loginbili 指令登录后重启")
    return@launch
  }
  listOf(launchCleanJob(), launchCollectJob()).joinAll()
}.apply {
  invokeOnCompletion { jobMap.clear() }
}

private fun CoroutineScope.launchCleanJob() = launch {
  while (isActive) {
    val sizeBefore = GuardList.count()
    logger.info { "Cleaning live guards..." }
    GuardList.cleanup()
    val now = GuardList.count()
    logger.info { "Cleaned live guards, size $sizeBefore -> $now" }
    delay(General.time.guardCleanup)
  }
}

private fun CoroutineScope.launchCollectJob() = launch {
  while (isActive) {
    delay(General.time.guardJobScan)
    val enabledMap = ListenerData.enabledMap
    val midsToListen = enabledMap.mapNotNull { it.value.userBind }

    logger.trace { "Refresh guard list fetch jobs..." }

    // cancel jobs
    jobMap.forEach { (mid, job) ->
      if (!midsToListen.contains(mid.toLong())) {
        job.cancel()
        jobMap.remove(mid)
      }
    }

    // enable jobs
    enabledMap.filterNot { it.value.userBind == null }.forEach { (_, data) ->
      val mid = data.userBind!!.toInt()
      if (jobMap.containsKey(mid)) return@forEach
      jobMap[mid] = launch job@{
        val retry = General.retryTimes

        val deferredId = async {
          retryCatching(retry) {
            client.getBasicInfo().data.mid ?: error("Failed to get self mid")
          }.onFailure {
            logger.warn { "Failed to get self mid after $retry times" }
          }.getOrNull()
        }

        val roomId = retryCatching(retry) {
          client.getRoomIdByUid(mid).roomId ?: error("Failed to get room id")
        }.onFailure {
          logger.warn(it) { "Failed to get room id of $mid after $retry times" }
        }.getOrNull() ?: return@job

        val realRoomId = retryCatching(retry) {
          client.getRoomInitInfo(roomId).data?.roomId ?: error("Failed to get room id")
        }.onFailure {
          logger.warn(it) { "Failed to get room id $mid after $retry times" }
        }.getOrNull() ?: return@job

        val stream = retryCatching(General.retryTimes) {
          client.getLiveDanmakuInfo(roomId).also {
            requireNotNull(it.data?.token)
            requireNotNull(it.data?.hostList)
          }
        }.onFailure {
          logger.warn(it) { "Failed to get message stream info after ${General.retryTimes} times" }
        }.getOrNull() ?: return@job

        val selfId = deferredId.await() ?: return@job

        UpdateRoomConnection(mid, roomId, realRoomId, selfId, stream).run { start() }
      }
    }
  }
}

class UpdateRoomConnection(
  private val mid: Int,
  private val roomId: Int,
  private val realId: Int,
  private val selfId: Int,
  private val stream: LiveDanmakuInfoGetResponse,
) {

  private val lastHeartbeatResp: AtomicRef<Instant?> = atomic(null)

  fun CoroutineScope.start() =
    launch(context = Dispatchers.IO + CoroutineName("live-message-fetcher")) job@{
      listOf(
        launchFetchAllJob(),
        connectLiveRoom(),
      ).joinAll()
    }

  private fun CoroutineScope.launchFetchAllJob(): Job =
    launch {
      while (isActive) {
        val lastList = GuardLastUpdate.get(mid.toLong())?.let {
          it[GuardLastUpdate.lastUpdate]
        }?.toKotlinInstant()
        if (lastList == null || (now() - lastList >= 1.toDuration(DurationUnit.DAYS))) {
          fetchAllGuardList(roomId, mid)
            .flowOn(Dispatchers.IO + CoroutineName("guard-lister-$mid"))
            .collect {
              GuardList.insertOrUpdate(mid.toLong(), it.uid?.toLong() ?: return@collect, after1Day, GuardFetcher.LIST)
            }
          GuardLastUpdate.insertOrUpdate(mid.toLong(), now())
        }
        delay(10_000)
      }
    }

  private fun CoroutineScope.connectLiveRoom() = launch {
    suspend fun createConnection() {
      runCatching {
        client.createLiveDanmakuConnection(
          selfId, realId, stream.data!!.token!!, stream.data!!.hostList.first()
        ) {
          onResponse()
        }
        while (isActive) {
          val time = lastHeartbeatResp.value
          if (time != null && now() - time >= General.time.reconnectNoResponse.toDuration(DurationUnit.MILLISECONDS)) {
            throw ReconnectException("Long time no response, try to reconnect")
          }
        }
      }.onFailure {
        when (it) {
          is CancellationException -> throw it
          is ReconnectException -> {
            logger.warn(it) { "Try to reconnect, because:" }
          }
          is UnresolvedAddressException -> {
            val delay = General.time.reconnectNoInternetMs
            logger.warn(it) { "Try to reconnect after $delay ms, because:" }
            delay(delay)
          }
          is IOException -> {
            val delay = General.time.reconnectIOExceptionMs
            logger.warn(it) { "Try to reconnect after $delay ms, because an IOException:" }
            delay(delay)
          }
          else -> {
            logger.warn(it) { "An exception occurred, try to reconnect" }
          }
        }
        createConnection()
      }
    }
    createConnection()
  }

  private fun LiveDanmakuConnectConfig.onResponse() {
    onCertificateResponse {
      logger.info { "Successfully connect to live room $roomId${if (roomId != realId) "($realId)" else ""}" }
    }

    onHeartbeatResponse { lastHeartbeatResp.getAndSet(now()) }

    onCommandResponse { flow ->
      flow.filterIsInstance<DanmakuMsgCmd>().collect {
        val roomId = it.data?.medal?.roomId ?: return@collect
        val lv = it.data?.medal?.level
        if (!(lv != null && lv >= 21)) return@collect
        if (roomId == this@UpdateRoomConnection.roomId || roomId == this@UpdateRoomConnection.realId)
          return@collect
        val uid = it.data?.liveUser?.mid ?: return@collect

        GuardList.insertOrUpdate(mid.toLong(), uid.toLong(), after1Day, GuardFetcher.MESSAGE)
      }

      flow.filterIsInstance<GuardBuyCmd>().collect { cmd ->
        val uid = cmd.data?.uid ?: return@collect
        GuardList.insertOrUpdate(mid.toLong(), uid.toLong(), after30Days, GuardFetcher.JOIN)
      }

      flow.filterIsInstance<SuperChatMsgCmd>().collect {
        val uid = it.data?.uid ?: return@collect
        val allowed = listOf(GuardLevel.CAPTAIN, GuardLevel.GOVERNOR, GuardLevel.ADMIRAL)
        if (!allowed.contains(it.data?.medalInfo?.guardLevel)) return@collect
        GuardList.insertOrUpdate(mid.toLong(), uid.toLong(), after1Day, GuardFetcher.MESSAGE)
      }
    }
  }
}

private fun fetchAllGuardList(roomId: Int, targetId: Int) = GuardListFetcher(roomId, targetId).fetchAllGuardList()

private class GuardListFetcher(
  private val roomId: Int,
  private val targetId: Int,
) {
  var listSize: Int? = null
    private set

  var maxPage: Int? = null
    private set

  fun fetchAllGuardList() = channelFlow {
    logger.info { "Starting fetch all guard list for liver $targetId" }
    var now = 0
    do {
      now++
      val resp = client.getGuardList(roomId, targetId, now)
      resp.data?.list.orEmpty().forEach { this.send(it) }
      if (maxPage == null) maxPage = resp.data?.info?.page
      if (listSize == null) listSize = resp.data?.info?.num
      delay(500)
    } while (now < (maxPage ?: 0))
    logger.info { "Fetched $maxPage page guards for liver $targetId" }
  }
}

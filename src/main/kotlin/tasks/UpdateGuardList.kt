package me.hbj.bikkuri.tasks

import io.ktor.utils.io.CancellationException
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
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
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import me.hbj.bikkuri.Bikkuri
import me.hbj.bikkuri.client
import me.hbj.bikkuri.data.General
import me.hbj.bikkuri.data.GuardData
import me.hbj.bikkuri.data.GuardFetcher
import me.hbj.bikkuri.data.GuardInfo
import me.hbj.bikkuri.data.ListenerData
import me.hbj.bikkuri.data.LiverGuard
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
import moe.sdl.yabapi.api.loginWebQRCodeInteractive
import moe.sdl.yabapi.connect.LiveDanmakuConnectConfig
import moe.sdl.yabapi.connect.onCertificateResponse
import moe.sdl.yabapi.connect.onCommandResponse
import moe.sdl.yabapi.connect.onHeartbeatResponse
import moe.sdl.yabapi.data.live.GuardLevel
import moe.sdl.yabapi.data.live.commands.DanmakuMsgCmd
import moe.sdl.yabapi.data.live.commands.GuardBuyCmd
import moe.sdl.yabapi.data.live.commands.SuperChatMsgCmd
import net.mamoe.mirai.console.util.retryCatching
import java.io.IOException
import java.nio.channels.UnresolvedAddressException
import kotlin.time.DurationUnit
import kotlin.time.toDuration

fun CoroutineScope.launchUpdateGuardListTask(): Job = launch {
  if (!client.getBasicInfo().data.isLogin) {
    Bikkuri.logger.info("检测到未登录，请根据流程扫码登录:")
    client.loginWebQRCodeInteractive {
      Bikkuri.logger.info(it)
    }
  }

  while (isActive) {
    delay(10_000)
    val enabledMap = ListenerData.enabledMap
    val midsToListen = enabledMap.mapNotNull { it.value.userBind }

    Bikkuri.logger.debug("Refresh guard list fetch jobs...")

    // cancel jobs
    jobMap.forEach { (mid, job) ->
      if (!midsToListen.contains(mid.toLong())) {
        job.cancel()
        jobMap.remove(mid)
      }
    }

    // enable jobs
    enabledMap.filterNot { it.value.userBind == null }.forEach { (_, data) ->
      val jobKey = data.userBind!!.toInt()
      if (jobMap.containsKey(jobKey)) return@forEach
      jobMap[jobKey] = launch(context = Dispatchers.IO + CoroutineName("live-message-fetcher")) job@{
        val retry = General.retryTimes

        val deferredId = async {
          retryCatching(retry) {
            client.getBasicInfo().data.mid ?: error("Failed to get self mid")
          }.onFailure {
            Bikkuri.logger.warning("Failed to get self mid after $retry times")
          }.getOrNull()
        }

        val roomId = retryCatching(retry) {
          client.getRoomIdByUid(jobKey).roomId ?: error("Failed to get room id")
        }.onFailure {
          Bikkuri.logger.warning("Failed to get room id of $jobKey after $retry times")
          Bikkuri.logger.warning(it)
        }.getOrNull() ?: return@job

        val realRoomId = retryCatching(retry) {
          client.getRoomInitInfo(roomId).data?.roomId ?: error("Failed to get room id")
        }.onFailure {
          Bikkuri.logger.warning("Failed to get room id $jobKey after $retry times")
          Bikkuri.logger.warning(it)
        }.getOrNull() ?: return@job

        val stream = retryCatching(retry) {
          client.getLiveDanmakuInfo(roomId).also {
            requireNotNull(it.data?.token)
            requireNotNull(it.data?.hostList)
          }
        }.onFailure {
          Bikkuri.logger.warning("Failed to get message stream info after $retry times")
          Bikkuri.logger.warning(it)
        }.getOrNull() ?: return@job

        val selfId = deferredId.await() ?: return@job

        launch {
          while (isActive) {
            val lastList = LiverGuard.map.getOrPut(jobKey) { GuardData() }.lastList
            if (lastList == null || (now() - lastList >= 1.toDuration(DurationUnit.DAYS))) {
              fetchAllGuardList(roomId, jobKey)
                .flowOn(Dispatchers.IO + CoroutineName("guard-lister-$jobKey"))
                .collect {
                  LiverGuard.updateGuard(jobKey, it.uid ?: return@collect, GuardInfo(after1Day, GuardFetcher.LIST))
                }
              LiverGuard.updateListTime(jobKey, now())
            }
            delay(10_000)
          }
        }

        launch {
          suspend fun createConnection() {
            runCatching {
              val lastHeartbeatResp: AtomicRef<Instant?> = atomic(null)
              client.createLiveDanmakuConnection(
                selfId, realRoomId, stream.data!!.token!!, stream.data!!.hostList.first()
              ) {
                onResponse(jobKey, roomId, realRoomId, lastHeartbeatResp)
              }
              while (isActive) {
                val time = lastHeartbeatResp.value
                if (time != null && now() - time >= 35.toDuration(DurationUnit.SECONDS)) {
                  throw ReconnectException("Long time no response, try to reconnect")
                }
              }
            }.onFailure {
              when(it) {
                is CancellationException -> throw it
                is ReconnectException -> {
                  Bikkuri.logger.warning("Try to reconnect, because:", it)
                }
                is UnresolvedAddressException -> {
                  Bikkuri.logger.warning("Try to reconnect after 5000 ms, because:", it)
                  delay(5000)
                }
                is IOException -> {
                  Bikkuri.logger.warning("Try to reconnect after 1000 ms, because an IOException:", it)
                  delay(1000)
                }
                else -> {
                  Bikkuri.logger.warning("An exception occurred, try to reconnect", it)
                }
              }
              createConnection()
            }
          }
          createConnection()
        }.join()
      }
    }
  }
}

internal val jobMap: MutableMap<Int, Job> = mutableMapOf()

fun LiveDanmakuConnectConfig.onResponse(liverMid: Int, shortId: Int, realId: Int, lastHeartbeatResp: AtomicRef<Instant?>) {
  onCertificateResponse {
    Bikkuri.logger.info("Successfully connect to live room $shortId${if (shortId != realId) "($realId)" else ""}")
  }
  onHeartbeatResponse { lastHeartbeatResp.getAndSet(now()) }
  onCommandResponse { flow ->
    flow.filterIsInstance<DanmakuMsgCmd>().collect {
      val roomId = it.data?.medal?.roomId ?: return@collect
      val lv = it.data?.medal?.level
      if (!(lv != null && lv >= 21)) return@collect
      if (roomId == shortId || roomId == realId) return@collect
      val uid = it.data?.liveUser?.mid ?: return@collect

      LiverGuard.updateGuard(liverMid, uid, GuardInfo(after1Day, GuardFetcher.MESSAGE))
    }

    flow.filterIsInstance<GuardBuyCmd>().collect { cmd ->
      val uid = cmd.data?.uid ?: return@collect
      val expiresAt = cmd.data!!.endTime?.let { Instant.fromEpochSeconds(it) } ?: after30Days
      LiverGuard.updateGuard(liverMid, uid, GuardInfo(expiresAt, GuardFetcher.JOIN))
    }

    flow.filterIsInstance<SuperChatMsgCmd>().collect {
      val uid = it.data?.uid ?: return@collect
      val allowed = listOf(GuardLevel.CAPTAIN, GuardLevel.GOVERNOR, GuardLevel.ADMIRAL)
      if (!allowed.contains(it.data?.medalInfo?.guardLevel)) return@collect
      LiverGuard.updateGuard(liverMid, uid, GuardInfo(after1Day, GuardFetcher.MESSAGE))
    }
  }
}

fun fetchAllGuardList(roomId: Int, targetId: Int) = GuardListFetcher(roomId, targetId).fetchAllGuardList()

private class GuardListFetcher(
  private val roomId: Int,
  private val targetId: Int,
) {
  var listSize: Int? = null
    private set

  var maxPage: Int? = null
    private set

  fun fetchAllGuardList() = channelFlow {
    Bikkuri.logger.info("Starting fetch all guard list for liver $targetId")
    var now = 0
    do {
      now++
      val resp = client.getGuardList(roomId, targetId, now)
      resp.data?.list.orEmpty().forEach { this.send(it) }
      if (maxPage == null) maxPage = resp.data?.info?.page
      if (listSize == null) listSize = resp.data?.info?.num
      delay(500)
    } while (now < (maxPage ?: 0))
    Bikkuri.logger.info("Fetched $maxPage page guards for liver $targetId")
  }
}

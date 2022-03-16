package me.hbj.bikkuri.data

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import me.hbj.bikkuri.util.now
import mu.KotlinLogging
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value

private val dataMutex = Mutex()

private val logger = KotlinLogging.logger {}

object LiverGuard : AutoSavePluginData("LiveGuardList") {
  // liver mid to GuardData
  val map: MutableMap<Int, GuardData> by value()

  suspend fun updateListTime(liverMid: Int, instant: Instant) = dataMutex.withLock {
    map.getOrPut(liverMid) { GuardData() }
    map[liverMid]?.lastList = instant
  }

  suspend fun updateGuard(liverMid: Int, guardMid: Int, new: GuardInfo) = dataMutex.withLock {
    val s by lazy { "Updating guard for user $liverMid guard $guardMid - $new" }
    if (new.from == GuardFetcher.LIST) {
      logger.trace { s }
    } else logger.info { s }
    map.getOrPut(liverMid) { GuardData() }.updateGuard(guardMid, new)
  }

  suspend fun getGuard(liverMid: Int, guardMid: Int) = dataMutex.withLock {
    map.getOrPut(liverMid) { GuardData() }.map[guardMid]
  }

  suspend fun cleanup() = dataMutex.withLock {
    map.forEach {
      it.value.cleanup()
    }
  }
}

@Serializable
data class GuardData(
  var lastList: Instant? = null,
  val map: MutableMap<Int, GuardInfo> = mutableMapOf()
) {
  @Transient
  private val mutex = Mutex()

  suspend fun updateGuard(guardMid: Int, new: GuardInfo) = mutex.withLock {
    if (!map.keys.contains(guardMid)) {
      map[guardMid] = new
    } else {
      val old = map[guardMid] ?: run {
        logger.warn { "Unexpected null when updateGuard map [$guardMid] to $new" }
        return@withLock
      }
      map[guardMid] = old.computeNewExpire(new.expiresAt, new.from)
    }
  }

  suspend fun cleanup() = mutex.withLock {
    val now = now()
    map.forEach { (k, v) ->
      if (v.expiresAt < now) {
        map.remove(k)
      }
    }
  }
}

@Serializable
enum class GuardFetcher {
  LIST, MESSAGE, JOIN;
}

@Serializable
data class GuardInfo(
  val expiresAt: Instant,
  val from: GuardFetcher
) {
  fun computeNewExpire(newInstant: Instant, from: GuardFetcher): GuardInfo =
    if (from >= this.from) this.copy(expiresAt = newInstant, from = from) else this
}

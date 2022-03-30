package me.hbj.bikkuri.data

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value

object General : AutoSavePluginConfig("_General") {
  val retryTimes by value(5)
  val joinRequiredLevel by value(21)
  val keygen by value(KeygenConfig())
  val time by value(TaskDelay())
}

@Serializable
data class KeygenConfig(
  val pattern: String = "123456789QWERTYUPASDFGHJKLZXCVBNMqwertyuiopasdfghjkzxcvbnm",
  val length: Int = 6,
  val timeout: Long = 300_000L, // s
)

@Serializable
data class TaskDelay(
  val timeout: Long = 5_000L,

  val responseTimeout: Long = 300_000L,

  val autoApprove: Long = 2_000L,

  val autoKick: Long = 5_000L,
  val messageNoticeBetweenKick: Long = 500L,

  val guardJobScan: Long = 1_000L,
  val guardCleanup: Long = 300_000L,

  val reconnectNoResponse: Long = 35_000L,
  val reconnectIOExceptionMs: Long = 1_000L,
  val reconnectNoInternetMs: Long = 5_000L,

  val replayDelayRange: Pair<Long, Long> = 0L to 0L,
)

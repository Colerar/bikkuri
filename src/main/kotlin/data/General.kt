package me.hbj.bikkuri.data

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value

object General : AutoSavePluginConfig("_General") {
  val retryTimes by value(5)
  val joinRequiredLevel by value(21)
  val keygen by value(KeygenConfig())
  val time by value(TaskDelay())
  val randomReply by value(RandomReply())
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
)

enum class RandomReplyMode {
  NONE, FIXED, BETWEEN, SCALE, MATH_LOG;
}

@Serializable
data class RandomReply(
  val mode: RandomReplyMode = RandomReplyMode.NONE,
  val fixedValue: Long = 100L,
  val betweenRange: Pair<Long, Long> = 100L to 1_000L,
  val scaleCoefficient: Pair<Double, Double> = 15.0 to 20.0,
  val log: Log = Log(),
) {
  @Serializable
  data class Log(
    val coefficient: Double = 20.0,
    val base: Double = 2.0,
    val pow: Int = 10,
    val constant: Double = 300.0,
    val jitter: Long = 300L,
  )
}

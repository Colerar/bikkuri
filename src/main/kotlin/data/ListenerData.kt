package me.hbj.bikkuri.data

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value

object ListenerData : AutoSavePluginData("ListenerData") {
  // Group ID to listener setting
  val map: MutableMap<Long, GroupListener> by value()

  val enabledMap
    get() = map.filter { it.value.enable }

  fun isEnabled(groupId: Long) = enabledMap.keys.contains(groupId)
}

@Serializable
data class GroupListener(
  var enable: Boolean = false,
  var userBind: Long? = null, // binds to bilibili mid
  var joinTimeLimit: UInt = 0u, // 0 for non limit
  var mode: ValidateMode = ValidateMode.SEND,
  var trigger: TimerTrigger = TimerTrigger.ON_MSG,
  var targetGroup: Long? = null,
  var kickDuration: ULong = 0uL,
)

@Serializable
enum class ValidateMode {
  RECV, SEND;
}

@Serializable
enum class TimerTrigger {
  ON_JOIN, ON_MSG;

  fun toFriendly(): String = when (this) {
    ON_JOIN -> "进群时重置"
    ON_MSG -> "发消息和进群时重置"
  }
  companion object {
    fun from(str: String): TimerTrigger? = when (str.lowercase()) {
      "msg" -> ON_MSG
      "join" -> ON_JOIN
      "on_msg" -> ON_MSG
      "on_join" -> ON_JOIN
      "进群" -> ON_JOIN
      "发消息" -> ON_MSG
      else -> null
    }
  }
}

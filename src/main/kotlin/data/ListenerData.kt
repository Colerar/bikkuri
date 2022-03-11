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
  var targetGroup: Long? = null,
  var kickDuration: ULong = 0uL,
)

package me.hbj.bikkuri.data

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value

object LastMsg : AutoSavePluginData("LastMsgList") {
  // group id to members data
  val map: MutableMap<Long, LastMsgData> by value()

  fun setToNow(groupId: Long, memberId: Long) {
    map.getOrPut(groupId) { LastMsgData() }.members[memberId] = Clock.System.now()
  }

  fun get(groupId: Long, memberId: Long): Instant {
    return map.getOrPut(groupId) { LastMsgData() }.members[memberId] ?: Clock.System.now()
  }

  fun remove(groupId: Long, memberId: Long) {
    map.getOrPut(groupId) { LastMsgData() }.members.remove(memberId)
  }
}

@Serializable
class LastMsgData(
  // member qq id to last msg
  val members: MutableMap<Long, Instant> = mutableMapOf()
)

package me.hbj.bikkuri.data

import java.util.concurrent.ConcurrentHashMap

object GlobalAutoApprove {
  // bot id to group map
  private val map: MutableMap<Long, BotApprove> = ConcurrentHashMap()

  operator fun get(botId: Long): BotApprove = map.getOrPut(botId) { BotApprove() }
}

class BotApprove {
  // group id to group list
  private val map: MutableMap<Long, GroupApprove> = ConcurrentHashMap()

  operator fun get(groupId: Long): GroupApprove = map.getOrPut(groupId) { GroupApprove() }
}

class GroupApprove {
  val map: MutableMap<Long, MemberToApprove> = ConcurrentHashMap()
}

data class MemberToApprove(
  val boundBiliUid: Long,
  val fromGroup: Long,
)

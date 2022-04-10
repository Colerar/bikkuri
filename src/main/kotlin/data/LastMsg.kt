package me.hbj.bikkuri.data

import kotlinx.datetime.Instant
import java.util.concurrent.ConcurrentHashMap

object GlobalLastMsg {
  // bot id to group map
  private val map: MutableMap<Long, BotLastMsg> = ConcurrentHashMap()

  operator fun get(botId: Long): BotLastMsg = map.getOrPut(botId) { BotLastMsg() }

  fun remove(botId: Long) = map.remove(botId)
}

class BotLastMsg {
  // group id to group list
  private val map: MutableMap<Long, GroupLastMsg> = ConcurrentHashMap()

  operator fun get(groupId: Long): GroupLastMsg = map.getOrPut(groupId) { GroupLastMsg() }

  fun remove(groupId: Long) = map.remove(groupId)
}

class GroupLastMsg {
  val map: MutableMap<Long, Instant> = ConcurrentHashMap()
}

package me.hbj.bikkuri.util

import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.EventChannel
import net.mamoe.mirai.event.events.GroupMessageEvent

fun <E : Event> EventChannel<E>.subscribeOnce(
  groupId: Long,
  userId: Long,
  extraFilter: ((E) -> Boolean)? = null,
  block: suspend (GroupMessageEvent) -> Unit,
) =
  filter {
    it is GroupMessageEvent &&
      it.group.id == groupId && it.sender.id == userId &&
      extraFilter?.invoke(it) != false
  }.subscribeOnce<GroupMessageEvent> {
    block(this)
  }

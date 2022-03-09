package me.hbj.bikkuri.events

import me.hbj.bikkuri.Bikkuri.logger
import me.hbj.bikkuri.data.AutoApprove
import me.hbj.bikkuri.data.AutoApproveData
import me.hbj.bikkuri.data.ListenerData
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.EventChannel
import net.mamoe.mirai.event.events.MemberJoinRequestEvent
import net.mamoe.mirai.utils.info

fun EventChannel<Event>.onMemberRequest() {
  subscribeAlways<MemberJoinRequestEvent> {
    val list = AutoApprove.map.getOrPut(groupId) { AutoApproveData() }.set
    if (list.contains(fromId)) {
      it.accept()
      AutoApprove.map[groupId]?.set?.remove(fromId)
      logger.info("Accepted MemberJoinRequestEvent because 'In auto approve list' for $this")
    }
  }
  subscribeAlways<MemberJoinRequestEvent> {
    if (!ListenerData.map.keys.contains(it.groupId)) return@subscribeAlways
    queuedMemberRequest.add(this)
    logger.info { "Add MemberJoinRequestEvent to queue: $this" }
  }
}

val queuedMemberRequest = mutableListOf<MemberJoinRequestEvent>()

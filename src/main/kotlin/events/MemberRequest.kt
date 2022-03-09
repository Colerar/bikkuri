package me.hbj.bikkuri.events

import me.hbj.bikkuri.data.AutoApprove
import me.hbj.bikkuri.data.AutoApproveData
import me.hbj.bikkuri.data.ListenerData
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.EventChannel
import net.mamoe.mirai.event.events.MemberJoinRequestEvent

fun EventChannel<Event>.onMemberRequest() {
  subscribeAlways<MemberJoinRequestEvent> {
    val list = AutoApprove.map.getOrPut(groupId) { AutoApproveData() }.set
    if (list.contains(fromId)) {
      it.accept()
      AutoApprove.map[groupId]?.set?.remove(fromId)
    }
  }
  subscribeAlways<MemberJoinRequestEvent> {
    if (!ListenerData.map.keys.contains(it.groupId)) return@subscribeAlways
    queuedMemberRequest.add(this)
  }
}

val queuedMemberRequest = mutableListOf<MemberJoinRequestEvent>()

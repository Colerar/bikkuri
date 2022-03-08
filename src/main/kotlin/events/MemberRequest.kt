package me.hbj.bikkuri.events

import me.hbj.bikkuri.data.AutoApprove
import me.hbj.bikkuri.data.AutoApproveData
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.EventChannel
import net.mamoe.mirai.event.events.MemberJoinRequestEvent

fun EventChannel<Event>.onMemberRequest() {
  subscribeAlways<MemberJoinRequestEvent> {
    val list = AutoApprove.map.getOrPut(groupId) { AutoApproveData() }.list
    if (list.contains(fromId)) {
      it.accept()
    }
  }
}

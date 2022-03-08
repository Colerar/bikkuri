package me.hbj.bikkuri.events

import me.hbj.bikkuri.data.ListenerData
import me.hbj.bikkuri.util.clearIndent
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.EventChannel
import net.mamoe.mirai.event.events.MemberJoinEvent

fun EventChannel<Event>.onNewMember() {
  subscribeAlways<MemberJoinEvent> {
    if (it.groupId !in ListenerData.map.keys) return@subscribeAlways
    if (ListenerData.map[it.groupId]?.enable == false) return@subscribeAlways
    group.sendMessage(
        """
            欢迎进入舰长审核群。输入 /sign 或者 /验证 开始审核哦~
            """.clearIndent()
    )
  }
}

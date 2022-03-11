package me.hbj.bikkuri.events

import me.hbj.bikkuri.data.LastMsg
import me.hbj.bikkuri.data.ListenerData
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.getMember
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.EventChannel
import net.mamoe.mirai.event.events.MemberJoinEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.buildMessageChain

fun EventChannel<Event>.onNewMember() {
  subscribeAlways<MemberJoinEvent> {
    if (it.groupId !in ListenerData.map.keys) return@subscribeAlways
    if (ListenerData.map[it.groupId]?.enable == false) return@subscribeAlways
    group.sendMessage(
      buildMessageChain {
        add(At(user))
        add(" 欢迎进入舰长审核群。输入 “/验证” 开始审核哦。")
      }
    )
    LastMsg.setToNow(groupId, member.id)
  }

  subscribeAlways<MemberJoinEvent> {
    val (sourceGroup, _) = ListenerData.map.toList().firstOrNull { (_, v) ->
      v.targetGroup == user.group.id
    } ?: return@subscribeAlways

    Bot.instances.forEach { bot ->
      bot.groups
        .filter { it.getMember(bot.id)?.isOperator() == true }
        .firstOrNull { it.id == sourceGroup }
        ?.getMember(member.id)?.kick("加入舰长群后自动从审核群移出")
    }
  }
}

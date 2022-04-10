package me.hbj.bikkuri.events

import me.hbj.bikkuri.data.GlobalLastMsg
import me.hbj.bikkuri.data.ListenerData
import me.hbj.bikkuri.db.isBlocked
import me.hbj.bikkuri.db.removeBlock
import me.hbj.bikkuri.util.now
import me.hbj.bikkuri.util.toFriendly
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.getMember
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.event.events.MemberJoinEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.buildMessageChain

fun Events.onMemberJoin() {
  subscribeAlways<MemberJoinEvent> {
    if (it.groupId !in ListenerData.map.keys) return@subscribeAlways
    if (ListenerData.map[it.groupId]?.enable == false) return@subscribeAlways
    group.sendMessage(
      buildMessageChain {
        add(At(user))
        add(" 欢迎进入舰长审核群。输入“验证”开始审核哦。")
      }
    )
    GlobalLastMsg[bot.id][groupId].map[member.id] = now()
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

  subscribeAlways<MemberJoinEvent.Invite> {
    if (member.isBlocked() && invitor.isOperator()) {
      member.removeBlock()
      group.sendMessage("管理邀请入群，自动将 ${member.toFriendly()} 移出屏蔽列表。")
    }
  }
}

package me.hbj.bikkuri.events

import kotlinx.coroutines.delay
import me.hbj.bikkuri.data.ListenerPersist
import me.hbj.bikkuri.db.isBlocked
import me.hbj.bikkuri.db.removeBlock
import me.hbj.bikkuri.utils.sendMessage
import me.hbj.bikkuri.utils.toFriendly
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.getMember
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.event.events.MemberJoinEvent
import net.mamoe.mirai.message.data.At

fun Events.onMemberJoin() {
  subscribeAlways<MemberJoinEvent> {
    if (!ListenerPersist.listeners.containsKey(groupId)) return@subscribeAlways

    val listener = ListenerPersist.listeners[groupId] ?: return@subscribeAlways
    if (!listener.enable) return@subscribeAlways

    delay(2_000) // 适当延迟, 给 QQ 客户端处理事件的时间, 避免没有 @ 到
    if (!it.group.contains(it.member.id)) return@subscribeAlways
    group.sendMessage {
      +At(user)
      +" 欢迎进入舰长审核群，我是自助审核机器人。输入“验证”开始审核哦。若在审核中遇到问题，请 @ 管理员。"
    }
  }

  subscribeAlways<MemberJoinEvent> { event ->
    val (sourceGroup, _) = ListenerPersist.listeners
      .asSequence()
      .filter { it.value.enable }
      .firstOrNull { it.value.targetGroup == event.group.id }
      ?: return@subscribeAlways

    Bot.instances.forEach { bot ->
      bot.groups
        .filter { it.getMember(bot.id)?.isOperator() == true }
        .firstOrNull { it.id == sourceGroup }
        ?.getMember(member.id)
        ?.kick("加入舰长群后自动从审核群移出")
    }
  }

  subscribeAlways<MemberJoinEvent.Invite> {
    if (member.isBlocked() && invitor.isOperator()) {
      member.removeBlock()
      group.sendMessage("管理邀请入群，自动将 ${member.toFriendly()} 移出屏蔽列表。")
    }
  }
}

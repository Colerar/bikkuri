package me.hbj.bikkuri.events

import me.hbj.bikkuri.data.GlobalLastMsg
import me.hbj.bikkuri.data.ListenerData
import me.hbj.bikkuri.db.addBlock
import me.hbj.bikkuri.db.isBlocked
import me.hbj.bikkuri.db.recordJoin
import me.hbj.bikkuri.db.removeBlock
import me.hbj.bikkuri.util.now
import me.hbj.bikkuri.util.toFriendly
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.contact.PermissionDeniedException
import net.mamoe.mirai.contact.getMember
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.event.events.MemberJoinEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.buildMessageChain

private val logger = mu.KotlinLogging.logger {}

fun Events.onMemberJoin() {
  subscribeAlways<MemberJoinEvent> {
    if (groupId !in ListenerData.map.keys) return@subscribeAlways
    val listener = ListenerData.map[groupId] ?: return@subscribeAlways
    if (!listener.enable) return@subscribeAlways

    GlobalLastMsg[bot.id][groupId].map[member.id] = now()
    run joinTimesLimit@{
      val joinTimes = user.recordJoin()
      if ((joinTimes >= listener.joinTimeLimit) && (joinTimes != 0u)) {
        user.addBlock()
        val kicked = kotlin.runCatching {
          (user as? NormalMember)?.kick("超过入群限制自动拉黑。")
        }.onFailure {
          if (it is PermissionDeniedException) {
            logger.info { "Bot ${bot.id} failed to kick member ${member.id}" }
          }
        }.getOrNull() != null
        if (kicked) {
          group.sendMessage("自动将 ${member.toFriendly()} 踢出并拉黑，因为超过了进群限额 $joinTimes/${listener.joinTimeLimit}")
        } else {
          group.sendMessage("因为超过了进群限额 $joinTimes，已将 ${member.toFriendly()} 拉黑，但机器人不是管理员。")
        }
        return@subscribeAlways
      }
    }

    group.sendMessage(
      buildMessageChain {
        add(At(user))
        add(" 欢迎进入舰长审核群。输入“验证”开始审核哦。")
      }
    )
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

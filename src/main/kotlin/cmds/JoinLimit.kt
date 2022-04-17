package me.hbj.bikkuri.cmds

import me.hbj.bikkuri.Bikkuri
import me.hbj.bikkuri.data.GroupListener
import me.hbj.bikkuri.data.ListenerData
import me.hbj.bikkuri.db.JoinTimes
import me.hbj.bikkuri.util.parseMessageMember
import me.hbj.bikkuri.util.requireOperator
import me.hbj.bikkuri.util.toFriendly
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.MemberCommandSender
import net.mamoe.mirai.message.data.MessageChain

private val logger = mu.KotlinLogging.logger {}

object JoinLimit :
  CompositeCommand(
    Bikkuri, "joinlimit", "limit"
  ),
  RegisteredCmd {
  @SubCommand("set")
  suspend fun MemberCommandSender.set(limit: String) {
    requireOperator(this)
    val id = group.id
    val data = ListenerData.map.getOrPut(id) { GroupListener(true) }
    val last = data.joinTimeLimit
    data.joinTimeLimit = limit.toUIntOrNull() ?: run {
      group.sendMessage("❌ 需输入非负整数, 0 代表不自动踢人")
      return
    }
    group.sendMessage("🔄 进群限制次数变化: $last -> $limit, 0 表示关闭")
    logger.debug { "GroupListener[$id] : ${ListenerData.map[id]}" }
  }

  @SubCommand("reset", "rm")
  suspend fun MemberCommandSender.reset(message: MessageChain) {
    requireOperator(this)
    fun delete(groupId: Long, memberId: Long): Boolean {
      return if (JoinTimes.contains(groupId, memberId)) {
        JoinTimes.delete(groupId, memberId)
        true
      } else false
    }
    parseMessageMember(
      message,
      onMember = {
        sendMessage(
          if (delete(group.id, it.id))
            "🔄 成功将 ${it.toFriendly()} 的进群记录重置。"
          else "🈳 成员 ${it.toFriendly()} 不存在进群记录。"
        )
      },
      onId = {
        sendMessage(
          if (delete(group.id, it))
            "🔄 成功将 $it 的进群记录重置。"
          else "🈳 成员 $it 不存在进群记录。"
        )
      }
    )
  }
}

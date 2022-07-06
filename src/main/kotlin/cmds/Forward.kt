package me.hbj.bikkuri.cmds

import me.hbj.bikkuri.Bikkuri
import me.hbj.bikkuri.db.ForwardRelation
import me.hbj.bikkuri.util.requireOperator
import me.hbj.bikkuri.util.toFriendly
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.MemberCommandSender
import net.mamoe.mirai.contact.getMember
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object Forward : CompositeCommand(Bikkuri, "forward", "fwd"), RegisteredCmd {
  private val comma = Regex("[,，]")

  @SubCommand
  suspend fun MemberCommandSender.info(): Unit = newSuspendedTransaction {
    requireOperator(this@info)
    val rel = ForwardRelation.findByIdOrNew(group.id)
    val msg = with(rel) {
      """
        ${if (enabled) "✅ " else "🚫 "}本群${if (enabled) "已开启" else "未开启"}转发功能
        是否转发所有人: ${if (forwardAll) "是" else "否"}
        转发群列表: ${toGroups.mapNotNull { bot.getGroup(it) }.joinToString { it.toFriendly(it.id) }}
        被转发人列表: ${forwardees.mapNotNull { group.getMember(it) }.joinToString { it.toFriendly() }}
      """.trimIndent()
    }
    sendMessage(msg)
  }

  @Suppress("UNCHECKED_CAST")
  @SubCommand
  suspend fun MemberCommandSender.set(forwardTo: String) {
    requireOperator(this)
    val toGroupIds = forwardTo.split(comma)
      .map { it.toLongOrNull() }
      .onEach {
        if (it == null) {
          sendMessage("❌ 输入错误，包括无效群号。")
          return
        }
      }.let { it as List<Long> }
    val groups = toGroupIds.map {
      bot.getGroup(it) ?: run {
        sendMessage("❌ 输入错误，包含无效或机器人不在的群。")
        return
      }
    }

    transaction {
      val rel = ForwardRelation.findByIdOrNew(id = group.id)
      with(rel.toGroups) {
        clear()
        addAll(toGroupIds)
      }
    }

    val groupStr = groups.joinToString { "${it.name} (${it.id})" }
    sendMessage("✅ 成功将要转发的群设置为: $groupStr")
  }

  @SubCommand
  suspend fun MemberCommandSender.clear() {
    requireOperator(this)
    transaction {
      val rel = ForwardRelation.findByIdOrNew(id = group.id)
      rel.toGroups.clear()
    }
    sendMessage("✅ 成功将转发列表清除。")
  }

  @SubCommand
  suspend fun MemberCommandSender.switch() {
    requireOperator(this)
    newSuspendedTransaction {
      val rel = ForwardRelation.findByIdOrNew(id = group.id)
      rel.enabled = !rel.enabled
      when (rel.enabled) {
        true -> sendMessage("✅ 已经开启转发功能")
        false -> sendMessage("🚫 已经关闭转发功能")
      }
    }
  }

  @SubCommand
  suspend fun MemberCommandSender.hint() {
    requireOperator(this)
    newSuspendedTransaction {
      val rel = ForwardRelation.findByIdOrNew(id = group.id)
      rel.showHint = !rel.showHint
      when (rel.showHint) {
        true -> sendMessage("✅ 将会在转发时显示说话人")
        false -> sendMessage("🚫 转发时将不会显示说话人")
      }
    }
  }

  @Suppress("UNCHECKED_CAST")
  @SubCommand
  suspend fun MemberCommandSender.forwardee(forwardee: String) {
    requireOperator(this)
    val forwardees = forwardee.split(comma)
      .map { it.toLongOrNull() }
      .onEach {
        if (it == null) {
          sendMessage("❌ 输入错误，包括无效QQ号。")
          return
        }
      }.let { it as List<Long> }
    val members = forwardees.map {
      group.getMember(it) ?: run {
        sendMessage("❌ 输入错误，输入的成员不在本群。")
        return
      }
    }

    transaction {
      val rel = ForwardRelation.findByIdOrNew(id = group.id)
      with(rel.forwardees) {
        clear()
        addAll(forwardees)
      }
    }

    val memberStr = members.joinToString { it.toFriendly() }
    sendMessage("✅ 成功将要转发的人设置为: $memberStr")
  }
}

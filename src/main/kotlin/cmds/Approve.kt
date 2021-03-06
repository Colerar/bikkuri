package me.hbj.bikkuri.cmds

import me.hbj.bikkuri.Bikkuri
import me.hbj.bikkuri.db.BotAccepted
import me.hbj.bikkuri.db.BotAccepted.boundBiliId
import me.hbj.bikkuri.db.BotAccepted.fromId
import me.hbj.bikkuri.util.parseMessageMember
import me.hbj.bikkuri.util.requireOperator
import me.hbj.bikkuri.util.toFriendly
import me.hbj.bikkuri.util.toLocalDateTime
import me.hbj.bikkuri.util.toReadDateTime
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.MemberCommandSender
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.data.MessageChain
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object Approve : CompositeCommand(Bikkuri, "approve", "ap"), RegisteredCmd {
  @SubCommand("queryqq")
  suspend fun MemberCommandSender.queryQQ(message: MessageChain) {
    requireOperator(this)
    parseMessageMember(
      message,
      onMember = { underlyingQueryQQ(it.id, it) },
      onId = { underlyingQueryQQ(it, null) }
    )
  }

  private suspend fun MemberCommandSender.underlyingQueryQQ(qq: Long, member: Member?) {
    val memberStr by lazy {
      member?.toFriendly() ?: qq.toString()
    }
    val list = transaction {
      val table = BotAccepted
      table.select {
        table.eq(bot, group) and table.eqMember(qq)
      }.toList().map {
        it[BotAccepted.instant].toLocalDateTime().toReadDateTime() to
          it[boundBiliId].toString()
      }
    }
    if (list.isEmpty()) {
      sendMessage("ðï¸ æªåç°å¯¹ $memberStr çè®°å½ã")
      return
    }
    val str = buildString {
      appendLine("ð æ¥è¯¢å° ${list.size} æ¡æ°æ®")
      appendLine("æ¥æ - ç»å®çBç«å¸å·uid")
      list.forEach {
        append(it.first ?: "unk")
        append(" - ")
        append(it.second)
      }
    }
    sendMessage(str)
  }

  @SubCommand("querybili", "queryb")
  suspend fun MemberCommandSender.queryBili(id: Long) {
    requireOperator(this)

    val list = transaction {
      val table = BotAccepted
      table.select {
        table.eq(bot, group) and table.eqBiliUser(id)
      }.toList().map {
        it[BotAccepted.instant].toLocalDateTime().toReadDateTime() to
          it[fromId].toString()
      }
    }
    if (list.isEmpty()) {
      sendMessage("ðï¸ æªåç°å¯¹ B ç« uid $id çè®°å½")
      return
    }
    val str = buildString {
      appendLine("ð æ¥è¯¢å° ${list.size} æ¡æ°æ®")
      appendLine("æ¥æ - ç»å®çQå·")
      list.forEach {
        append(it.first)
        append(" - ")
        append(it.second)
      }
    }
    sendMessage(str)
  }
}

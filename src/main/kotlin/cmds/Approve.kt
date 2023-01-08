package me.hbj.bikkuri.cmds

import kotlinx.datetime.toJavaInstant
import me.hbj.bikkuri.Bikkuri
import me.hbj.bikkuri.data.ListenerData
import me.hbj.bikkuri.db.BotAccepted
import me.hbj.bikkuri.db.BotAccepted.boundBiliId
import me.hbj.bikkuri.db.BotAccepted.fromId
import me.hbj.bikkuri.util.now
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
import org.jetbrains.exposed.sql.insert
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

  @SubCommand("add")
  suspend fun MemberCommandSender.add(bili: Long, qq: Long) {
    requireOperator(this)
    val sender = this
    val data = ListenerData.map[group.id]
    if (data == null || !data.enable) {
      group.sendMessage("当前群未启用验证")
      return
    }
    val targetGroup = data.targetGroup
    if (targetGroup == null) {
      group.sendMessage("当前群未设置目标群")
      return
    }
    val result = transaction {
      BotAccepted.insert {
        it[instant] = now().toJavaInstant()
        it[botId] = bot.id
        it[fromId] = qq
        it[boundBiliId] = bili
        it[fromGroupId] = sender.group.id
        it[toGroupId] = targetGroup
      }
    }
    if (result.insertedCount <= 1) {
      group.sendMessage("✅ 添加成功")
    } else {
      group.sendMessage("❌ 添加失败")
    }
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
      sendMessage("🈚️ 未发现对 $memberStr 的记录。")
      return
    }
    val str = buildString {
      appendLine("🔍 查询到 ${list.size} 条数据")
      appendLine("日期 - 绑定的B站帐号uid")
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
      sendMessage("🈚️ 未发现对 B 站 uid $id 的记录")
      return
    }
    val str = buildString {
      appendLine("🔍 查询到 ${list.size} 条数据")
      appendLine("日期 - 绑定的Q号")
      list.forEach {
        append(it.first)
        append(" - ")
        append(it.second)
      }
    }
    sendMessage(str)
  }
}

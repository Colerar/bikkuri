package me.hbj.bikkuri.commands

import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.toJavaInstant
import me.hbj.bikkuri.command.Command
import me.hbj.bikkuri.command.MiraiCommandSender
import me.hbj.bikkuri.commands.Approve.Operation.*
import me.hbj.bikkuri.commands.Approve.Operation.Add
import me.hbj.bikkuri.data.ListenerPersist
import me.hbj.bikkuri.db.ApproveLink
import me.hbj.bikkuri.db.BotAccepted
import me.hbj.bikkuri.db.BotAccepted.boundBiliId
import me.hbj.bikkuri.db.BotAccepted.fromId
import me.hbj.bikkuri.db.redirectApproveLink
import me.hbj.bikkuri.utils.now
import me.hbj.bikkuri.utils.toLocalDateTime
import me.hbj.bikkuri.utils.toReadDateTime
import moe.sdl.yac.core.PrintMessage
import moe.sdl.yac.parameters.groups.OptionGroup
import moe.sdl.yac.parameters.groups.groupSwitch
import moe.sdl.yac.parameters.groups.required
import moe.sdl.yac.parameters.options.convert
import moe.sdl.yac.parameters.options.help
import moe.sdl.yac.parameters.options.option
import moe.sdl.yac.parameters.options.required
import moe.sdl.yac.parameters.types.long
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class Approve(private val sender: MiraiCommandSender) : Command(Approve) {
  val member = memberOperator(sender)

  sealed class Operation : OptionGroup() {
    class Add : Operation() {
      val qq by option("--qq").long().help("要查询的 QQ 号").required()
      val bili by option("--bili").long().help("要查询的 B 站 UID").required()
      val source by option("--from").long().help("审核群号, 默认为当前群（可能有重定向）")
      val target by option("--to").long().help("目标群号, 默认为当前群配置的目标群")
    }

    class QueryQQ : Operation() {
      val qq by option("-q").help("要查询的 QQ 号").convert {
        convertAt(it)
      }.required()
    }

    class QueryBili : Operation() {
      val bili by option("-b").long().help("要查询的 B 站 UID").required()
    }

    class Redirect : Operation() {
      val source by option("--source").long().help("原群号").required()
      val target by option("--target").long().help("重定向群号").required()
    }

    class RemoveRedirect : Operation() {
      val source by option("--rm").long().help("原群号").required()
    }
  }

  val operation by option().groupSwitch(
    "--queryqq" to QueryQQ(),
    "--querybili" to QueryBili(),
    "--add" to Add(),
    "--redirect" to Redirect(),
    "--remove-redirect" to RemoveRedirect(),
  ).required()

  override suspend fun run(): Unit = coroutineScope cmd@{
    when (val op = operation) {
      is Operation.Add -> {
        val approveId = op.source ?: member.group.redirectApproveLink()
        val target = op.target
          ?: ListenerPersist.listeners[approveId]?.targetGroup
          ?: throw PrintMessage("未输入目标群号")
        val result = transaction {
          BotAccepted.insert {
            it[instant] = now().toJavaInstant()
            it[botId] = member.bot.id
            it[fromId] = op.qq
            it[boundBiliId] = op.bili
            it[fromGroupId] = approveId
            it[toGroupId] = target
          }
        }
        if (result.insertedCount <= 1) {
          sender.sendMessage("✅ 添加成功")
        } else {
          sender.sendMessage("❌ 添加失败")
        }
      }

      is QueryBili -> {
        val list = transaction {
          val table = BotAccepted
          table.select {
            table.eq(member.bot.id, member.group.redirectApproveLink()) and table.eqBiliUser(op.bili)
          }.toList().map {
            it[BotAccepted.instant].toLocalDateTime().toReadDateTime() to
              it[fromId].toString()
          }
        }
        if (list.isEmpty()) {
          sender.sendMessage("🈚️ 未发现对 B 站 uid ${op.bili} 的记录")
          return@cmd
        }
        val str = buildString {
          appendLine("🔍 查询到 ${list.size} 条数据")
          appendLine("日期 - 绑定的Q号")
          list.forEach {
            append(it.first)
            append(" - ")
            append(it.second)
            appendLine()
          }
        }
        sender.sendMessage(str)
      }

      is QueryQQ -> {
        val qq = op.qq
        val list = transaction {
          val table = BotAccepted
          table.select {
            table.eq(member.bot.id, member.group.redirectApproveLink()) and table.eqMember(qq)
          }.toList().map {
            it[BotAccepted.instant].toLocalDateTime().toReadDateTime() to
              it[boundBiliId].toString()
          }
        }
        if (list.isEmpty()) {
          sender.sendMessage("🈚️ 未发现对 $qq 的记录。")
          return@cmd
        }
        val str = buildString {
          appendLine("🔍 查询到 ${list.size} 条数据")
          appendLine("日期 - 绑定的B站帐号uid")
          list.forEach {
            append(it.first ?: "unk")
            append(" - ")
            append(it.second)
            appendLine()
          }
        }
        sender.sendMessage(str)
      }

      is Redirect -> {
        ApproveLink.link(op.source, op.target)
        sender.sendMessage("重定向成功")
      }

      is RemoveRedirect -> {
        ApproveLink.remove(op.source)
        sender.sendMessage("移除重定向成功")
      }
    }
  }

  companion object : Entry(
    name = "approve",
    help = "操作和查询加群历史",
    alias = listOf("ap"),
  )
}

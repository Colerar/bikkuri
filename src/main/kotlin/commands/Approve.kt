package me.hbj.bikkuri.commands

import kotlinx.datetime.toJavaInstant
import me.hbj.bikkuri.command.Command
import me.hbj.bikkuri.command.MiraiCommandSender
import me.hbj.bikkuri.commands.Query.Op.Bili
import me.hbj.bikkuri.commands.Query.Op.QQ
import me.hbj.bikkuri.data.ListenerPersist
import me.hbj.bikkuri.db.ApproveLink
import me.hbj.bikkuri.db.BotAccepted
import me.hbj.bikkuri.db.BotAccepted.boundBiliId
import me.hbj.bikkuri.db.BotAccepted.fromId
import me.hbj.bikkuri.db.redirectApproveLink
import me.hbj.bikkuri.utils.lazyUnsafe
import me.hbj.bikkuri.utils.now
import me.hbj.bikkuri.utils.toLocalDateTime
import me.hbj.bikkuri.utils.toReadDateTime
import moe.sdl.yac.core.PrintMessage
import moe.sdl.yac.core.subcommands
import moe.sdl.yac.parameters.groups.mutuallyExclusiveOptions
import moe.sdl.yac.parameters.groups.required
import moe.sdl.yac.parameters.groups.single
import moe.sdl.yac.parameters.options.*
import moe.sdl.yac.parameters.types.long
import net.mamoe.mirai.contact.NormalMember
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class Approve(sender: MiraiCommandSender) : Command(
  Approve,
  Option(printHelpOnEmptyArgs = true),
) {
  companion object : Entry(
    name = "approve",
    help = "操作和查询加群历史",
    alias = listOf("ap"),
  )

  val member = memberOperator(sender)

  init {
    subcommands(
      Query(sender, member),
      ApproveAdd(sender, member),
      Redirect(sender, member),
    )
  }

  override suspend fun run() = Unit
}

private class ApproveAdd(
  val sender: MiraiCommandSender,
  val member: NormalMember,
) : Command(ApproveAdd) {
  companion object : Entry(
    name = "add",
    help = "添加记录",
    alias = listOf("a"),
  )

  val qq by option("--qq", "-q").long().help("要查询的 QQ 号").required()
  val bili by option("--bili", "-b").long().help("要查询的 B 站 UID").required()
  val source by option("--from").long().help("审核群号, 默认为当前群（可能有重定向）")
  val target by option("--to").long().help("目标群号, 默认为当前群配置的目标群")

  override suspend fun run() {
    val approveId = source ?: member.group.redirectApproveLink()
    val target = target
      ?: ListenerPersist.listeners[approveId]?.targetGroup
      ?: throw PrintMessage("未输入目标群号")
    val result = transaction {
      BotAccepted.insert {
        it[instant] = now().toJavaInstant()
        it[botId] = member.bot.id
        it[fromId] = qq
        it[boundBiliId] = bili
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
}

private class Query(
  val sender: MiraiCommandSender,
  val member: NormalMember,
) : Command(Query) {
  companion object : Entry(
    name = "query",
    help = "查询机器人审批记录",
    alias = listOf("q"),
  )

  private enum class Op {
    QQ, Bili,
  }

  private val operation by mutuallyExclusiveOptions(
    option("--qq", "-q").help("要查询的 QQ 号").convert { QQ to convertAt(it) },
    option("--bili", "-b").long().help("要查询的 B 站 UID").convert {
      Bili to it
    },
  ).single().required()

  override suspend fun run() {
    val (type, num) = operation
    when (type) {
      QQ -> onQQ(num)
      Bili -> onBili(num)
    }
  }

  suspend fun onQQ(qq: Long) {
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
      return
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

  suspend fun onBili(bili: Long) {
    val list = transaction {
      val table = BotAccepted
      table.select {
        table.eq(member.bot.id, member.group.redirectApproveLink()) and table.eqBiliUser(bili)
      }.toList().map {
        it[BotAccepted.instant].toLocalDateTime().toReadDateTime() to
          it[fromId].toString()
      }
    }
    if (list.isEmpty()) {
      sender.sendMessage("🈚️ 未发现对 B 站 uid $bili 的记录")
      return
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
}

private class Redirect(
  val sender: MiraiCommandSender,
  val member: NormalMember,
) : Command(Redirect) {
  val source by option("--source", "-s").long().help("原群号, 默认为当前群").default(member.group.id)
  val target by option("--target", "-t").long().help("重定向群号")
  val remove by option("--remove", "-R").help("删除重定向").flag()

  companion object : Entry(
    name = "redirect",
    help = "设置/删除查询时的重定向",
    alias = listOf("r", "ln", "link"),
  )

  override suspend fun run() {
    val query by lazyUnsafe { ApproveLink.query(source) }

    // remove
    if (remove) {
      if (query == null) {
        sender.sendMessage("不存在重定向")
        return
      }

      ApproveLink.remove(source)
      sender.sendMessage("删除成功")
      return
    }

    // insert
    val to = target ?: throw PrintMessage("请通过 --target 输入重定向后的群号")
    if (query != null) {
      sender.sendMessage("已存在重定向，请先删除")
    }
    member.bot.getGroup(to) ?: throw PrintMessage("机器人不在目标群 $to 或目标群不存在，请重新输入")
    ApproveLink.link(source, to)
    sender.sendMessage("重定向成功")
  }
}

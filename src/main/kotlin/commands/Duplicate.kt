package me.hbj.bikkuri.commands

import me.hbj.bikkuri.command.Command
import me.hbj.bikkuri.command.MiraiCommandSender
import me.hbj.bikkuri.db.DupAllow
import me.hbj.bikkuri.utils.checkDuplicate
import me.hbj.bikkuri.utils.lazyUnsafe
import me.hbj.bikkuri.utils.sendMessage
import me.hbj.bikkuri.utils.toTreeString
import moe.sdl.yac.core.PrintMessage
import moe.sdl.yac.core.subcommands
import moe.sdl.yac.parameters.options.*
import moe.sdl.yac.parameters.types.long
import moe.sdl.yac.parameters.types.restrictTo
import net.mamoe.mirai.contact.NormalMember
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteIgnoreWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

class Duplicate(sender: MiraiCommandSender) : Command(Duplicate) {
  companion object : Entry(
    name = "duplicate",
    help = "查重群员",
    alias = listOf("dup"),
  )

  val member = memberOperator(sender)

  init {
    subcommands(
      DupCheck(member),
      DupAllowlist(member),
    )
  }

  override suspend fun run() = Unit
}

private class DupCheck(
  val member: NormalMember,
) : Command(DupCheck) {
  companion object : Entry(
    name = "check",
    help = "检测重复进群情况",
  )

  val groups by option("--groups", "-g", help = "要检测的群号").convert { ids ->
    ids.split(',')
      .map { it.toLongOrNull() ?: throw PrintMessage("输入的群号不是数字") }
      .map { member.bot.getGroup(it) ?: throw PrintMessage("机器人未加入群聊 $it") }
      .toSet()
  }.required().check("应当至少输入两个群聊以供查重") {
    it.size > 1
  }

  override suspend fun run() {
    val whitelist = transaction {
      DupAllow
        .selectAll()
        .limit(100)
        .map { it[DupAllow.id].value }
    }
    val dups = newSuspendedTransaction {
      member.bot.checkDuplicate(groups, whitelist)
    }
    if (dups.isEmpty()) {
      member.group.sendMessage("无重复进群的成员")
      return
    }
    member.group.sendMessage("以下为重复进群的群员:\n${dups.toTreeString()}")
  }
}

private class DupAllowlist(
  val member: NormalMember,
) : Command(DupAllowlist) {
  companion object : Entry(
    name = "allowlist",
    help = "更改白名单设置",
    alias = listOf("al", "wl", "whitelist"),
  )

  val ids by option("--qq", "-q", help = "要操作的 QQ 号").convert { ids ->
    ids.split(',').map { convertAt(it) }.toSet()
  }

  enum class Op {
    Add,
    Remove,
    List,
  }

  val op by option().switch(
    "--add" to Op.Add,
    "--remove" to Op.Remove,
    "--list" to Op.List,
    "-a" to Op.Add,
    "-r" to Op.Remove,
    "-l" to Op.List,
  ).required()

  val page by option("--page", "-p").help("要查看的页码").long().restrictTo(1, 100).default(1)

  override suspend fun run() {
    val ids by lazyUnsafe { ids ?: throw PrintMessage("该操作需要指定 --uid 参数") }
    when (op) {
      Op.Add -> {
        val inserted = transaction {
          DupAllow.batchInsert(ids, ignore = true) { qq ->
            this[DupAllow.id] = qq
          }.map {
            it[DupAllow.id].value
          }
        }
        if (ids.size == inserted.size) {
          member.group.sendMessage("添加白名单成功！共添加 ${ids.size} 人。")
        } else {
          val failed = ids.subtract(inserted.toSet())
          member.group.sendMessage("添加白名单成功！共添加 ${inserted.size}/${ids.size} 人。添加失败：$failed")
        }
      }

      Op.Remove -> {
        transaction {
          ids.map { qq ->
            DupAllow.deleteIgnoreWhere { id.eq(qq) }
          }
        }
        member.group.sendMessage("删除白名单成功！")
      }

      Op.List -> {
        val listed = transaction {
          DupAllow
            .selectAll()
            .limit(10, (page - 1) * 10)
            .map { it[DupAllow.id].value }
        }
        member.group.sendMessage("白名单包括这些成员：${listed.joinToString("、")}")
      }
    }
  }
}

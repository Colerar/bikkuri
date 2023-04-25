package me.hbj.bikkuri.commands

import me.hbj.bikkuri.command.Command
import me.hbj.bikkuri.command.MiraiCommandSender
import me.hbj.bikkuri.db.*
import me.hbj.bikkuri.utils.lazyUnsafe
import me.hbj.bikkuri.utils.toFriendly
import me.hbj.bikkuri.utils.toLocalDateTime
import me.hbj.bikkuri.utils.toReadDateTime
import moe.sdl.yac.core.CliktCommand
import moe.sdl.yac.core.PrintMessage
import moe.sdl.yac.core.context
import moe.sdl.yac.core.subcommands
import moe.sdl.yac.parameters.arguments.argument
import moe.sdl.yac.parameters.arguments.convert
import moe.sdl.yac.parameters.arguments.default
import moe.sdl.yac.parameters.arguments.optional
import moe.sdl.yac.parameters.types.long
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.contact.getMember
import kotlin.math.max

class Block(private val sender: MiraiCommandSender) : Command(Block, Option(printHelpOnEmptyArgs = true)) {
  val member = memberOperator(sender)

  init {
    context {
      subcommands(
        List(sender, member),
        Link(sender, member),
        Add(member),
        Remove(member),
        BiliAdd(member),
        BiliRemove(member),
      )
    }
  }

  override suspend fun run() {}

  companion object : Entry(
    name = "block",
    help = "屏蔽 QQ 或 B 站用户",
    alias = listOf("b"),
  )
}

private class List(
  val sender: MiraiCommandSender,
  val member: NormalMember,
) : CliktCommand("查看当前屏蔽列表") {
  val page by argument("页码").long().default(1)
  val group = member.group

  override suspend fun run() {
    val size = 10
    val maxPage = group.blockedSize() / size + 1
    if (page !in 1..max(1L, maxPage)) {
      sender.sendMessage("❌ 页码超出范围")
      return
    }
    if (maxPage <= 0) {
      sender.sendMessage("🈚️ 当前无拦截名单")
      return
    }
    val sb = StringBuilder()
    sb.appendLine("📝 当前拦截名单(按添加时间排序):")
    group.listBlocked(page, size).forEach { (id, time) ->
      val member = group.getMember(id)
      sb.appendLine("${member?.toFriendly() ?: id} - ${time.toLocalDateTime().toReadDateTime()}")
    }
    sb.appendLine("$page / $maxPage")
    sender.sendMessage(sb.toString())
  }
}

private class Add(
  val member: NormalMember,
) : CliktCommand() {
  val id by argument("QQ", help = "要屏蔽的 QQ 号").long()
  val group = member.group
  override suspend fun run() {
    if (group.isBlocked(id)) {
      group.sendMessage("❌ 用户 $id 已经位于拦截名单中。")
    } else {
      group.addBlock(id)
      group.sendMessage("✅ 成功将 $id 添加至拦截列表。")
    }
  }
}

private class BiliAdd(
  val member: NormalMember,
) : CliktCommand() {
  val id by argument("UID", help = "要屏蔽的 B 站 UID").long()
  val group = member.group
  override suspend fun run() {
    if (group.isBiliBlocked(id)) {
      group.sendMessage("❌ 用户 $id 已经位于拦截名单中。")
    } else {
      group.addBiliBlock(id)
      group.sendMessage("✅ 成功将 $id 添加至拦截列表。")
    }
  }
}

private class Remove(
  val member: NormalMember,
) : CliktCommand() {
  val id by argument("QQ", help = "要屏蔽的 QQ 号").long()
  val group = member.group
  override suspend fun run() {
    if (!group.isBlocked(id)) {
      group.sendMessage("❌ 用户 $id 不在拦截名单中。")
    } else {
      group.removeBlock(id)
      group.sendMessage("✅ 成功将 $id 移出拦截列表。")
    }
  }
}

private class BiliRemove(
  val member: NormalMember,
) : CliktCommand() {
  val id by argument("UID", help = "要屏蔽的 B 站 UID").long()
  val group = member.group
  override suspend fun run() {
    if (!group.isBiliBlocked(id)) {
      group.sendMessage("❌ 用户 $id 不在拦截名单中。")
    } else {
      group.removeBiliBlock(id)
      group.sendMessage("✅ 成功将 $id 移出拦截列表。")
    }
  }
}

private class Link(
  val sender: MiraiCommandSender,
  val member: NormalMember,
) : CliktCommand() {
  val bot = member.bot
  val group = member.group
  val source = member.group
  val operator by argument("操作")
  val target by argument("重定向后的群聊").long().convert {
    member.bot.getGroup(it) ?: throw PrintMessage("机器人未加入该群聊，或该群聊不存在: $it")
  }.optional()

  override suspend fun run() {
    val fromStr = source.toFriendly()
    val toStr = target.toFriendly()

    // 操作符判断
    fun op(vararg matches: String) = matches.contains(operator)
    val toBefore by lazyUnsafe { BlocklistLink.query(source.id) }

    when {
      op("link", "add") -> {
        if (toBefore == null) {
          if (target?.id == null) throw PrintMessage("需要输入重定向到哪个群聊")
          BlocklistLink.link(source.id, target!!.id)
          sender.sendMessage("✅ 成功设置拦截名单重定向 $fromStr 🔗 $toStr")
        } else {
          val beforeGroup = bot.getGroup(toBefore!!).toFriendly(toBefore)
          sender.sendMessage("🤔 已有重定向 $fromStr 🔗 $beforeGroup 存在。考虑使用 update 命令进行更新。")
        }
      }

      op("update", "upd") -> {
        if (toBefore != null) {
          if (target?.id == null) throw PrintMessage("需要输入重定向到哪个群聊")
          BlocklistLink.link(source.id, target!!.id)
          sender.sendMessage("✅ 成功修改拦截名单重定向 $fromStr 🔗 $toStr")
        } else {
          sender.sendMessage("🈳 本群没有拦截列表重定向。")
        }
      }

      else -> sender.sendMessage("❌ 不存在此操作: $operator")
    }
  }
}

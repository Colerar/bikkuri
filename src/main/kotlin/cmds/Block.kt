package me.hbj.bikkuri.cmds

import me.hbj.bikkuri.Bikkuri
import me.hbj.bikkuri.db.BlocklistLink
import me.hbj.bikkuri.db.BotAccepted
import me.hbj.bikkuri.db.addBiliBlock
import me.hbj.bikkuri.db.addBlock
import me.hbj.bikkuri.db.blockedSize
import me.hbj.bikkuri.db.blockedTime
import me.hbj.bikkuri.db.isBlocked
import me.hbj.bikkuri.db.listBlocked
import me.hbj.bikkuri.db.removeBlock
import me.hbj.bikkuri.util.clearIndent
import me.hbj.bikkuri.util.kickAll
import me.hbj.bikkuri.util.parseMessageMember
import me.hbj.bikkuri.util.require
import me.hbj.bikkuri.util.requireOperator
import me.hbj.bikkuri.util.toFriendly
import me.hbj.bikkuri.util.toLocalDateTime
import me.hbj.bikkuri.util.toReadDateTime
import me.hbj.bikkuri.util.toTreeString
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.MemberCommandSender
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.contact.getMember
import net.mamoe.mirai.contact.isAdministrator
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.buildMessageChain
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import kotlin.math.max

object Block :
  CompositeCommand(
    Bikkuri, "blocklist", "block", "b"
  ),
  RegisteredCmd {
  override val usage: String = """
    /blocklist 屏蔽指令，缩写 /block /b
    <> 表示必需参数 [] 表示可选参数
    /block help 显示帮助页面
    /block list/ls [页码] 查看当前屏蔽列表
    /block add <At|QQ号> 添加某人到屏蔽列表
    /block kick <At|QQ号> 添加某人到屏蔽列表，同时移出本群
    /block ban <At|QQ号> 添加某人到屏蔽列表，移出本群，同时使用QQ的拉黑功能
    /block remove/rm <At|QQ号> 将某人移出屏蔽列表
  """.trimIndent()

  @SubCommand("help")
  suspend fun MemberCommandSender.help() {
    requireOperator(this)
    group.sendMessage(usage)
  }

  @SubCommand("add")
  suspend fun MemberCommandSender.add(message: MessageChain) {
    requireOperator(this)
    underlyingAdd(message, kick = false, block = false)
  }

  @SubCommand("kick")
  suspend fun MemberCommandSender.kick(message: MessageChain) {
    requireOperator(this)
    underlyingAdd(message, kick = true, block = false)
  }

  @SubCommand("ban")
  suspend fun MemberCommandSender.ban(message: MessageChain) {
    requireOperator(this)
    underlyingAdd(message, kick = true, block = true)
  }

  @SubCommand("banbili")
  suspend fun MemberCommandSender.banbili(id: Long) {
    requireOperator(this)
    newSuspendedTransaction {
      buildMessageChain l@{
        group.addBiliBlock(id)
        add("成功将 B 站用户 $id 添加至拦截名单。")
        val membersToKick = mutableListOf<NormalMember>()
        // val kickedMembers = mutableListOf<NormalMember>()
        val relatedGroups = BlocklistLink.related(bot, group)
        val relatedMembers = BotAccepted.select {
          (BotAccepted.toGroupId inList relatedGroups.map { it.id }) and
            BotAccepted.eqBiliUser(id)
        }.map {
          it[BotAccepted.fromId]
        }
        relatedGroups.forEach { group ->
          if (!group.botPermission.isAdministrator()) return@l
          val toKick = relatedMembers.mapNotNull { group.getMember(it) }
          membersToKick.addAll(toKick)
          toKick.forEach {
            if (!it.isBlocked()) it.addBlock()
          }
        }
        membersToKick.kickAll(reason = "已被拉黑")
        add("同时踢出并拉黑了以下成员:\n")
        add(membersToKick.toTreeString().trimEnd())
      }.also {
        sendMessage(it)
      }
    }
  }

  private suspend fun MemberCommandSender.underlyingAdd(message: MessageChain, kick: Boolean, block: Boolean) {
    parseMessageMember(
      message,
      onMember = { member ->
        require(this@underlyingAdd, !member.isOperator()) { "❌ 你没有权限！" }
        if (member.isBlocked()) {
          group.sendMessage("❌ 成员 ${member.toFriendly()} 已经位于拦截名单中。")
        } else {
          member.addBlock()
          val extraMessage = if (kick) {
            if (group.getMember(bot.id)?.isOperator() == true) {
              member.kick("", block)
              "，同时将其移出本群。"
            } else "，机器人没有管理员权限，无法踢出该成员。"
          } else "。"
          group.sendMessage("✅ 成功将 ${member.toFriendly()} 添加至拦截列表$extraMessage")
        }
      },
      onId = { id ->
        if (group.isBlocked(id)) {
          group.sendMessage("❌ 用户 $id 已经位于拦截名单中。")
        } else {
          group.addBlock(id)
          group.sendMessage("✅ 成功将 $id 添加至拦截列表。")
        }
      }
    )
  }

  @SubCommand("remove", "rm")
  suspend fun MemberCommandSender.remove(message: MessageChain) {
    requireOperator(this)
    parseMessageMember(
      message,
      onMember = { member ->
        if (member.isBlocked()) {
          member.removeBlock()
          group.sendMessage("✅ 成功将 ${member.toFriendly()} 移出拦截列表。")
        } else {
          group.sendMessage("❌ 成员 ${member.toFriendly()} 不存在于拦截列表。")
        }
      },
      onId = { id ->
        if (group.isBlocked(id)) {
          group.removeBlock(id)
          group.sendMessage("✅ 成功将 $id 移出拦截列表。")
        } else {
          group.sendMessage("❌ 成员 $id 不存在于拦截列表。")
        }
      }
    )
  }

  @SubCommand("query")
  suspend fun MemberCommandSender.query(message: MessageChain) {
    requireOperator(this)
    var id: Long? = null
    parseMessageMember(
      message,
      onMember = { id = it.id },
      onId = { id = it }
    )
    id ?: return
    val memberStr = group[id!!]?.toFriendly() ?: id.toString()
    if (group.isBlocked(id!!)) {
      val date = group.blockedTime(id!!)?.toLocalDateTime()?.toReadDateTime() ?: "未知时间"
      group.sendMessage("🔍 $memberStr 在拦截名单中，添加于 $date。")
    } else {
      group.sendMessage("🔍 $memberStr 不在拦截名单中。")
    }
  }

  @SubCommand("list", "ls")
  suspend fun MemberCommandSender.list() = underlyingList()

  @SubCommand("list", "ls")
  suspend fun MemberCommandSender.list(page: Long) = underlyingList(page)

  private suspend fun MemberCommandSender.underlyingList(page: Long = 1) {
    requireOperator(this)
    val size = 10
    val maxPage = group.blockedSize() / size + 1
    if (page !in 1..max(1L, maxPage)) {
      group.sendMessage("❌ 页码超出范围")
      return
    }
    if (maxPage <= 0) {
      group.sendMessage("🈚️ 当前无拦截名单")
      return
    }
    val sb = StringBuilder()
    sb.appendLine("📝 当前拦截名单(按添加时间排序):")
    group.listBlocked(page, size).forEach { (id, time) ->
      val member = group.getMember(id)
      sb.appendLine("${member?.toFriendly() ?: id} - ${time.toLocalDateTime().toReadDateTime()}")
    }
    sb.appendLine("$page / $maxPage")
    group.sendMessage(sb.toString().clearIndent())
  }

  @SubCommand("link")
  suspend fun MemberCommandSender.link(operator: String, to: Long) {
    requireOperator(this)

    val from = user.group.id
    val fromGroup = bot.getGroup(from)
    val toGroup = bot.getGroup(to)

    if (!checkIsAdmin(fromGroup, toGroup)) {
      sendMessage("❌ 需要在两个群都为管理才能设置本选项。")
      return
    }

    val fromStr = fromGroup.toFriendly(from)
    val toStr = toGroup.toFriendly(to)

    // 操作符判断
    fun op(vararg matches: String) = matches.contains(operator)
    val toBefore by lazy { BlocklistLink.query(from) }

    when {
      op("link", "add") -> {
        if (toBefore == null) {
          BlocklistLink.link(from, to)
          sendMessage("✅ 成功设置拦截名单重定向 $fromStr 🔗 $toStr")
        } else {
          val beforeGroup = bot.getGroup(toBefore!!).toFriendly(toBefore)
          sendMessage("🤔 已有重定向 $fromStr 🔗 $beforeGroup 存在。考虑使用 update 命令进行更新。")
        }
      }
      op("update", "upd") -> {
        if (toBefore != null) {
          if (!checkIsAdmin(toBefore!!)) {
            sendMessage("⚠️ 需要在之前绑定的群也为管理员，才能修改重定向。")
            return
          }
          BlocklistLink.update(from, to)
          sendMessage("✅ 成功修改拦截名单重定向 $fromStr 🔗 $toStr")
        } else {
          sendMessage("🈳 本群没有拦截列表重定向。")
        }
      }
      else -> sendMessage("❌ 错误的操作输入: $operator")
    }
  }

  @SubCommand("link")
  suspend fun MemberCommandSender.link(operator: String) {
    requireOperator(this)

    val from = user.group.id
    val fromGroup = bot.getGroup(from)
    val fromStr = fromGroup.toFriendly(from)

    val toBefore by lazy { BlocklistLink.query(from) }

    fun op(vararg matches: String) = matches.contains(operator)
    when {
      op("rm", "remove") -> {
        if (toBefore != null) {
          val toStr = bot.getGroup(toBefore!!).toFriendly(toBefore)
          if (!checkIsAdmin(from, toBefore!!)) {
            sendMessage("⚠️ 需要在 $toStr 也为管理才能移除重定向。")
            return
          }
          BlocklistLink.remove(from)
          sendMessage("💥 成功移除拦截名单重定向 $fromStr 🚧 $toStr")
        } else {
          sendMessage("🈳 本群没有拦截列表重定向。")
          return
        }
      }
      op("now", "see") -> {
        if (toBefore == null) {
          sendMessage("🈳 本群没有拦截列表重定向。")
          return
        }
        val linkStr = bot.getGroup(toBefore!!).toFriendly(toBefore)
        sendMessage("🔍 当前拦截列表重定向 $fromStr 🔗 $linkStr")
      }
      else -> sendMessage("❌ 错误的操作输入: $operator")
    }
  }

  private fun MemberCommandSender.checkIsAdmin(vararg group: Long) =
    checkIsAdmin(*group.map(bot::getGroup).toTypedArray())

  private fun MemberCommandSender.checkIsAdmin(vararg group: Group?) =
    group.fold(true) { acc, i ->
      acc && i?.getMember(this.user.id)?.isOperator() == true
    }
}

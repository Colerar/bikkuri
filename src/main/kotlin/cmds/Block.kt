package me.hbj.bikkuri.cmds

import me.hbj.bikkuri.Bikkuri
import me.hbj.bikkuri.db.addBlock
import me.hbj.bikkuri.db.blockedSize
import me.hbj.bikkuri.db.blockedTime
import me.hbj.bikkuri.db.isBlocked
import me.hbj.bikkuri.db.listBlocked
import me.hbj.bikkuri.db.removeBlock
import me.hbj.bikkuri.util.clearIndent
import me.hbj.bikkuri.util.parseMessageMember
import me.hbj.bikkuri.util.require
import me.hbj.bikkuri.util.requireOperator
import me.hbj.bikkuri.util.toFriendly
import me.hbj.bikkuri.util.toLocalDateTime
import me.hbj.bikkuri.util.toReadDateTime
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.MemberCommandSender
import net.mamoe.mirai.contact.getMember
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.message.data.MessageChain
import kotlin.math.max

object Block : CompositeCommand(
  Bikkuri, "blocklist", "block", "/b"
) {
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

  private suspend fun MemberCommandSender.underlyingAdd(message: MessageChain, kick: Boolean, block: Boolean) {
    parseMessageMember(
      message,
      onMember = { member ->
        require(this@underlyingAdd, !member.isOperator()) { "你没有权限！" }
        if (member.isBlocked()) {
          group.sendMessage("成员 ${member.toFriendly()} 已经位于拦截名单中。")
        } else {
          member.addBlock()
          val extraMessage = if (kick) {
            if (group.getMember(bot.id)?.isOperator() == true) {
              member.kick("", block)
              "，同时将其移出本群。"
            } else "，机器人没有管理员权限，无法踢出该成员。"
          } else "。"
          group.sendMessage("成功将 ${member.toFriendly()} 添加至拦截列表$extraMessage")
        }
      },
      onId = { id ->
        if (group.isBlocked(id)) {
          group.sendMessage("用户 $id 已经位于拦截名单中。")
        } else {
          group.addBlock(id)
          group.sendMessage("成功将 $id 添加至拦截列表。")
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
          group.sendMessage("成功将 ${member.toFriendly()} 移出拦截列表。")
        } else {
          group.sendMessage("成员 ${member.toFriendly()} 不存在于拦截列表。")
        }
      },
      onId = { id ->
        if (group.isBlocked(id)) {
          group.removeBlock(id)
          group.sendMessage("成功将 $id 移出拦截列表。")
        } else {
          group.sendMessage("成员 $id 不存在于拦截列表。")
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
      val date = group.blockedTime(id!!)?.toLocalDateTime()?.toReadDateTime() ?: "未知"
      group.sendMessage("$memberStr 在拦截名单中，添加于 $date。")
    } else {
      group.sendMessage("$memberStr 在拦截名单中。")
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
      group.sendMessage("页码超出范围")
      return
    }
    if (maxPage <= 0) {
      group.sendMessage("当前无拦截名单")
      return
    }
    val sb = StringBuilder()
    sb.appendLine("当前拦截名单(按添加时间排序):")
    group.listBlocked(page, size).forEach { (id, time) ->
      val member = group.getMember(id)
      sb.appendLine("${member?.toFriendly() ?: id} - ${time.toLocalDateTime().toReadDateTime()}")
    }
    sb.appendLine("$page / $maxPage")
    group.sendMessage(sb.toString().clearIndent())
  }
}

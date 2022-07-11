package me.hbj.bikkuri.cmds

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeoutOrNull
import me.hbj.bikkuri.Bikkuri
import me.hbj.bikkuri.db.DuplicateAllowlist
import me.hbj.bikkuri.db.DuplicateConfig
import me.hbj.bikkuri.db.DuplicateTable
import me.hbj.bikkuri.exception.command.CommandCancellation
import me.hbj.bikkuri.util.checkDuplicate
import me.hbj.bikkuri.util.getMemberByGroups
import me.hbj.bikkuri.util.kickAll
import me.hbj.bikkuri.util.parseTime
import me.hbj.bikkuri.util.requireOperator
import me.hbj.bikkuri.util.toFriendly
import me.hbj.bikkuri.util.toTreeString
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.MemberCommandSender
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.nextEvent
import net.mamoe.mirai.message.data.content
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.time.toJavaDuration

object Duplicate : CompositeCommand(Bikkuri, "duplicate", "dup"), RegisteredCmd {
  @SubCommand
  suspend fun MemberCommandSender.new() {
    requireOperator(this)
    newSuspendedTransaction {
      val id = DuplicateTable.insertAndGetId {}
      val config = DuplicateConfig.findById(id)
      group.sendMessage("➕ 成功添加一组去重任务：\n${config?.toFriendly(true)}")
    }
  }

  private suspend fun MemberCommandSender.findDupConfig(id: Int) =
    DuplicateConfig.findById(id) ?: run {
      sendMessage("❌ 无法找到 ID '$id' 对应的去重任务")
      throw CancellationException("Cannot find duplicate task of id $id")
    }

  @SubCommand("addgroup")
  suspend fun MemberCommandSender.addgroup(id: Int, group: String) {
    requireOperator(this)
    val groups = group.split(',').map {
      it.toLongOrNull() ?: run {
        sendMessage("❌ 输入含有无效群号")
        return
      }
    }.onEach {
      bot.getGroup(it) ?: run {
        sendMessage("❌ 输入含有机器人不在或无效的群")
        return
      }
    }
    newSuspendedTransaction {
      val config = findDupConfig(id)
      runCatching {
        config.groups.addAll(groups)
      }.onSuccess {
        sendMessage(
          """
          ✅ 添加成功，当前配置：
          ${config.toFriendly()}
          """.trimIndent()
        )
      }.onFailure {
        sendMessage("❌ 添加失败")
      }
    }
  }

  @SubCommand("removegroup", "rmgroup")
  suspend fun MemberCommandSender.rmgroup(id: Int, group: String) {
    requireOperator(this)
    newSuspendedTransaction {
      val config = findDupConfig(id)
      if (group == "ALL") {
        config.groups.clear()
      } else {
        config.groups.removeAll(group.split(',').map { it.toLongOrNull() }.toSet())
      }
      sendMessage("✅ 成功删除关联群")
    }
  }

  private suspend fun MemberCommandSender.parseQQId(string: String) =
    string.split(',').map {
      it.toLongOrNull() ?: run {
        sendMessage("❌ 输入含有无效QQ号")
        throw CommandCancellation(Duplicate)
      }
    }

  private fun MemberCommandSender.getMembersStr(
    memberIds: Collection<Long>,
    config: DuplicateConfig,
  ) = memberIds.joinToString { id ->
    val groups = buildSet {
      add(group)
      addAll(config.groups.mapNotNull { bot.getGroup(it) })
    }
    getMemberByGroups(id, groups)?.toFriendly() ?: id.toString()
  }

  @SubCommand("addwl", "addwhitelist")
  suspend fun MemberCommandSender.addwl(dupId: Int, members: String) {
    requireOperator(this)
    val parsedIds = parseQQId(members)
    newSuspendedTransaction {
      val config = findDupConfig(dupId)
      runCatching {
        config.allowed.addAll(parsedIds.filterNot { config.allowed.contains(it) })
      }.onSuccess {
        val memberStr = getMembersStr(parsedIds, config)
        sendMessage("已将以下成员添加到去重白名单: $memberStr")
      }.onFailure {
        sendMessage("❌ 添加失败")
      }.getOrThrow()
    }
  }

  @SubCommand("rmwl", "removewl")
  suspend fun MemberCommandSender.removeWhitelist(dupId: Int, members: String) {
    requireOperator(this)
    val parsedIds = parseQQId(members)
    newSuspendedTransaction {
      val config = findDupConfig(dupId)
      runCatching {
        config.allowed.removeAll(parsedIds.filter { config.allowed.contains(it) })
      }.onSuccess {
        val memberStr = getMembersStr(parsedIds, config)
        sendMessage("已将以下成员移出去重白名单: $memberStr")
      }.onFailure {
        sendMessage("❌ 移除失败")
      }.getOrThrow()
    }
  }

  @SubCommand("haswhitelist", "haswl")
  suspend fun MemberCommandSender.haswl(id: Int, qqs: String) {
    requireOperator(this)
    newSuspendedTransaction {
      val config = findDupConfig(id)
      val result = qqs.split(',').mapNotNull {
        it.toLongOrNull()
      }.map {
        it to DuplicateAllowlist.hasMember(id, it)
      }.filter {
        it.second
      }.joinToString { (id, _) ->
        val groups = buildSet {
          add(group)
          addAll(config.groups.mapNotNull { bot.getGroup(it) })
        }
        getMemberByGroups(id, groups)?.toFriendly() ?: id.toString()
      }
      sendMessage(
        buildString {
          appendLine("位于白名单中的成员:")
          append(result)
        }
      )
    }
  }

  @SubCommand("delete", "del")
  suspend fun MemberCommandSender.del(id: Int) {
    requireOperator(this)
    val config = newSuspendedTransaction {
      DuplicateConfig.findById(id) ?: run {
        sendMessage("ID 不存在。")
        throw CommandCancellation(Duplicate)
      }
    }
    val send = newSuspendedTransaction {
      sendMessage("您将要删除以下去重任务，是否确认 [Y/n]?\n${config.toFriendly()}")
    }
    val event = withTimeoutOrNull(30_000) {
      GlobalEventChannel.nextEvent<GroupMessageEvent> { it.sender == this@del.user }
    }
    if (event == null) {
      send.quoteReply("删除询问会话超时，现已退出。")
      throw CommandCancellation(Duplicate)
    }
    if (event.message.content == "Y") {
      newSuspendedTransaction {
        runCatching {
          config.delete()
        }.onSuccess {
          group.sendMessage("✅ 已成功删除去重任务 $id")
        }.onFailure {
          group.sendMessage("删除失败……")
        }.getOrThrow()
      }
    } else group.sendMessage("已取消")
  }

  @SubCommand
  suspend fun MemberCommandSender.info(id: Int) {
    requireOperator(this)
    newSuspendedTransaction {
      sendMessage(findDupConfig(id).toFriendly(longMessage = true))
    }
  }

  @SubCommand("list", "ls")
  suspend fun MemberCommandSender.list() {
    requireOperator(this)
    val msg = transaction {
      val tasks = DuplicateTable.selectAll().map { it[DuplicateTable.id] }.mapNotNull {
        DuplicateConfig.findById(it)
      }
      buildString {
        appendLine(if (tasks.isEmpty()) "暂无去重任务。" else "有 ${tasks.count()} 个去重任务:")
        append(
          tasks.joinToString("\n") {
            it.toFriendly()
          }
        )
      }
    }
    group.sendMessage(msg)
  }

  @SubCommand
  suspend fun MemberCommandSender.switch(id: Int) {
    requireOperator(this)
    newSuspendedTransaction {
      val config = findDupConfig(id)
      config.enabled = !config.enabled
      sendMessage(if (config.enabled) "✅ 去重任务已开启" else "🚫 去重任务已关闭")
    }
  }

  @SubCommand("dur", "duration")
  suspend fun MemberCommandSender.duration(id: Int, duration: String) {
    requireOperator(this)
    newSuspendedTransaction l@{
      val config = findDupConfig(id)
      val dur = parseTime(duration) ?: run {
        sendMessage("❌ 解析表达式失败")
        return@l
      }
      if (dur.inWholeMinutes < 5) {
        sendMessage("❌ 输入的间隔太小")
        return@l
      }
      if (dur.inWholeDays > 1) {
        sendMessage("❌ 输入的间隔太大")
        return@l
      }
      config.checkInterval = dur.toJavaDuration()
      sendMessage("✅ 已将任务间隔设为 ${dur.toFriendly()} 一次")
    }
  }

  @SubCommand
  suspend fun MemberCommandSender.check(id: Int) {
    requireOperator(this)
    newSuspendedTransaction l@{
      val config = findDupConfig(id)
      val groups = config.groups.mapNotNull { bot.getGroup(it) }
      val toKick = bot.checkDuplicate(groups, config.allowed)
      if (toKick.isEmpty()) {
        sendMessage("无重复进群的群员。")
        return@l
      }
      val msg = "将要踢出以下成员，输入 Y 确认:\n${toKick.toTreeString()}".trimEnd()
      val msgReceipt = sendMessage(msg)
      val event = withTimeoutOrNull(30_000) {
        GlobalEventChannel.nextEvent<GroupMessageEvent> { it.sender == this@check.user }
      }
      if (event == null) {
        msgReceipt.quoteReply("询问会话超时，现已退出。")
      } else if (event.message.content == "Y") {
        toKick.kickAll()
        group.sendMessage("✅ 成功踢出重复群员")
      } else {
        group.sendMessage("已取消")
      }
    }
  }
}

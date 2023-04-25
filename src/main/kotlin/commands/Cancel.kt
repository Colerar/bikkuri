package me.hbj.bikkuri.commands

import kotlinx.coroutines.coroutineScope
import me.hbj.bikkuri.command.Command
import me.hbj.bikkuri.command.JobIdentity
import me.hbj.bikkuri.command.MiraiCommandSender
import me.hbj.bikkuri.events.commandCtxManager
import moe.sdl.yac.core.PrintMessage
import moe.sdl.yac.parameters.arguments.argument
import moe.sdl.yac.parameters.arguments.convert
import moe.sdl.yac.parameters.arguments.optional
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.contact.getMember
import net.mamoe.mirai.contact.isOperator

class Cancel(private val sender: MiraiCommandSender) : Command(Cancel) {
  val at by argument("AT", "要取消谁正在执行的命令?")
    .convert { convertAt(it) }
    .optional()

  override suspend fun run(): Unit = coroutineScope cmd@{
    if (sender.contact !is NormalMember) return@cmd
    val group = sender.contact.group
    val memberId = if (at == null) {
      sender.contact.id
    } else {
      if (!sender.contact.isOperator()) return@cmd
      group.getMember(at!!)?.id ?: throw PrintMessage("不存在该成员 @$at")
    }

    val jobId = JobIdentity(
      sender.contact.bot.id,
      group.id,
      memberId,
    )
    val contains = commandCtxManager.ctxMap.containsKey(jobId)
    if (contains) {
      if (!sender.contact.isOperator()) return@cmd
      commandCtxManager.cancelJob(jobId)
      sender.sendMessage("已取消正在执行的命令")
    } else {
      sender.sendMessage("没有在执行命令")
    }
  }

  companion object : Entry(
    name = "cancel",
    help = "取消命令执行",
  )
}

package me.hbj.bikkuri.commands

import dev.inmo.krontab.buildSchedule
import dev.inmo.krontab.doInfinity
import dev.inmo.krontab.nextOrNow
import korlibs.time.jvm.toDate
import kotlinx.coroutines.launch
import me.hbj.bikkuri.command.Command
import me.hbj.bikkuri.command.MiraiCommandSender
import me.hbj.bikkuri.data.BackupTaskPersist
import me.hbj.bikkuri.tasks.autoBackupModule
import me.hbj.bikkuri.tasks.autoBackups
import me.hbj.bikkuri.tasks.backup
import me.hbj.bikkuri.utils.toLocalDateTime
import me.hbj.bikkuri.utils.toReadDateTime
import moe.sdl.yac.core.PrintMessage
import moe.sdl.yac.core.subcommands
import moe.sdl.yac.parameters.arguments.argument
import moe.sdl.yac.parameters.arguments.help
import moe.sdl.yac.parameters.arguments.optional
import moe.sdl.yac.parameters.options.flag
import moe.sdl.yac.parameters.options.help
import moe.sdl.yac.parameters.options.option
import mu.KotlinLogging
import net.mamoe.mirai.contact.NormalMember

private val logger = KotlinLogging.logger {}

class Backup(sender: MiraiCommandSender) : Command(
  Backup,
  Option(printHelpOnEmptyArgs = true),
) {
  companion object : Entry(
    name = "backup",
    help = "备份群成员",
  )

  val member = memberOperator(sender)

  init {
    subcommands(BackupRun(member))
    subcommands(BackupTask(member))
    subcommands(BackupNext(member))
  }

  override suspend fun run() = Unit
}

private class BackupRun(private val member: NormalMember) : Command(BackupRun) {
  companion object : Entry(
    name = "run",
    help = "立刻运行备份任务",
    alias = listOf("r"),
  )

  override suspend fun run() {
    member.group.backup()
  }
}

private class BackupTask(private val member: NormalMember) : Command(BackupTask) {
  companion object : Entry(
    name = "task",
    help = "设置定时任务",
    alias = listOf("t"),
  )

  val cron by argument("CRON 表达式").help("时区为 GMT+0").optional()
  val remove by option("-R", "--remove").help("移除定时任务").flag()

  override suspend fun run() {
    val backups = BackupTaskPersist.backups
    if (remove) {
      val msg = when (backups.remove(member.group.id)) {
        null -> "当前不存在定时任务"
        else -> "成功删除定时任务"
      }
      autoBackups[member.group.id]?.cancel()
      autoBackups.remove(member.group.id)
      member.group.sendMessage(msg)
      return
    }
    val cron = cron ?: throw PrintMessage("未输入 CRON 表达式")
    val expr = try {
      buildSchedule(cron)
    } catch (e: Exception) {
      logger.debug(e) { "Failed to create cron" }
      throw PrintMessage("设置失败，CRON 表达式有误: $e")
    }
    val next = expr.nextOrNow()
    val nextStr = next.toDate().toInstant().toLocalDateTime().toReadDateTime()
    val backup = me.hbj.bikkuri.data.Backup(cron)
    backups[member.group.id] = backup
    member.group.sendMessage("成功设置定时任务，下次运行于：$nextStr（服务器时间）")
    autoBackupModule?.launch {
      doInfinity(backup.cron) {
        member.group.backup()
      }
    }?.apply {
      autoBackups[member.group.id] = this
      invokeOnCompletion { autoBackups.remove(member.group.id) }
    }
  }
}

private class BackupNext(private val member: NormalMember) : Command(BackupNext) {
  companion object : Entry(
    name = "next",
    help = "查看定时任务何时运行",
    alias = listOf("n"),
  )

  override suspend fun run() {
    val backups = BackupTaskPersist.backups
    val backup = backups.computeIfAbsent(member.group.id) {
      throw PrintMessage("当前群没有定时任务。")
    }
    val cron = backup.cron
    val expr = buildSchedule(cron)
    val next = expr.nextOrNow()
    val nextStr = next.toDate().toInstant().toLocalDateTime().toReadDateTime()
    member.group.sendMessage("当前的表达式为 $cron，下次运行于：$nextStr（服务器时间）")
  }
}

package me.hbj.bikkuri.cmds

import com.cronutils.model.Cron
import me.hbj.bikkuri.Bikkuri
import me.hbj.bikkuri.data.BackupTask
import me.hbj.bikkuri.data.BackupTasks
import me.hbj.bikkuri.tasks.MemberBackupTask
import me.hbj.bikkuri.util.Formatter
import me.hbj.bikkuri.util.nextExecutionTime
import me.hbj.bikkuri.util.parseCron
import me.hbj.bikkuri.util.requireOperator
import me.hbj.bikkuri.util.toFriendly
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.MemberCommandSender
import net.mamoe.mirai.contact.Group

object Backup : CompositeCommand(Bikkuri, "backup"), RegisteredCmd {
  @SubCommand("run")
  suspend fun MemberCommandSender.run() {
    requireOperator(this)
    group.backup()
  }

  private suspend fun MemberCommandSender.addTask(cron: Cron) =
    BackupTasks.add(BackupTask(cron, true, group.id, bot.id))

  @SubCommand("task")
  suspend fun MemberCommandSender.task(msg: String) {
    requireOperator(this)
    val expr = msg.replace('|', ' ')
    val cron = parseCron(expr) ?: run {
      sendMessage("❌ 表达式输入有误。")
      return
    }

    val task = BackupTasks.get(bot, group)

    task?.let {
      BackupTasks.remove(it)
      addTask(cron)
    } ?: run {
      addTask(cron)
    }
    sendMessage("✅ 已经成功设置定时任务，预计下次运行于 ${cron.nextExecutionTime()?.toFriendly(formatter = Formatter.dateTime2)}")
  }

  @SubCommand("rmtask", "removetask")
  suspend fun MemberCommandSender.removeTask() {
    requireOperator(this)
    if (BackupTasks.remove(bot, group)) {
      sendMessage("💥 已成功移除定时任务。")
    } else sendMessage("🈚️ 本群无定时任务。")
  }

  @SubCommand("see", "now", "next")
  suspend fun MemberCommandSender.next() {
    requireOperator(this)
    val cron = BackupTasks.get(bot, group)?.cron
    val nextTime = cron?.nextExecutionTime()?.toFriendly(formatter = Formatter.dateTime2)
    val msg = if (nextTime == null) {
      "🈚️ 本群暂无定时任务"
    } else "⏱ 预计下次备份于 $nextTime, 目前 cron 表达式为 ${cron.asString()}"
    sendMessage(msg)
  }
}

suspend fun Group.backup() {
  sendMessage("⏱ 开始备份群员列表……")
  val task = MemberBackupTask(this).apply {
    run()
  }
  if (task.savedMember == 0) {
    sendMessage("❌ 备份时发生错误，详情请查看后台")
  } else sendMessage("✅ 备份完成! 已保存 ${task.savedMember} 名群员。")
}

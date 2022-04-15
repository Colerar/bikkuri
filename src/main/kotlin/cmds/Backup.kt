package me.hbj.bikkuri.cmds

import com.cronutils.model.Cron
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
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

  @SubCommand("task")
  suspend fun MemberCommandSender.task(msg: String) {
    requireOperator(this)
    val expr = msg.replace('|', ' ')
    val cron = parseCron(expr) ?: run {
      sendMessage("表达式输入有误。")
      return
    }

    val task = BackupTasks.lock.withLock {
      BackupTasks.set.asSequence()
        .filter { it.botId == bot.id }
        .firstOrNull { it.groupId == group.id }
    }

    task?.let {
      BackupTasks.lock.withLock {
        BackupTasks.set.remove(it)
      }
      addTask(cron)
    } ?: run {
      addTask(cron)
    }
    sendMessage("已经成功设置定时任务，预计下次运行于 ${cron.nextExecutionTime()?.toFriendly(formatter = Formatter.dateTime2)}")
  }

  private suspend fun MemberCommandSender.addTask(cron: Cron) = BackupTasks.lock.withLock {
    BackupTasks.set.add(BackupTask(cron, true, group.id, bot.id))
  }
}

suspend fun Group.backup() {
  sendMessage("开始备份群员列表……")
  val task = MemberBackupTask(this).apply {
    withContext(Dispatchers.IO) {
      run()
    }
  }
  sendMessage("备份完成! 已保存 ${task.savedMember} 名群员。")
}

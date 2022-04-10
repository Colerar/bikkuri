package me.hbj.bikkuri.cmds

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.hbj.bikkuri.Bikkuri
import me.hbj.bikkuri.tasks.MemberBackupTask
import me.hbj.bikkuri.util.requireOperator
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.MemberCommandSender
import net.mamoe.mirai.message.data.buildMessageChain

object Backup : CompositeCommand(Bikkuri, "backup") {
  @SubCommand("run")
  suspend fun MemberCommandSender.run() {
    requireOperator(this)
    sendMessage("开始备份群员列表……")
    val task = MemberBackupTask(group).apply {
      withContext(Dispatchers.IO) {
        run()
      }
    }
    sendMessage(
      buildMessageChain {
        // add(QuoteReply(message))
        add("备份完成! 已保存 ${task.saveMember} 名群员。")
      }
    )
  }
}

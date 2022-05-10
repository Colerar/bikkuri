package me.hbj.bikkuri.tasks

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.hbj.bikkuri.cmds.backup
import me.hbj.bikkuri.data.BackupTasks
import me.hbj.bikkuri.data.General
import mu.KotlinLogging
import net.mamoe.mirai.Bot

private val logger = KotlinLogging.logger {}

fun CoroutineScope.launchBackupJob(): Job = launch {
  while (isActive) {
    delay(General.time.backupScan)
    BackupTasks.getAll().asFlow().buffer().map { task ->
      task.withDo {
        val bot = Bot.instances.firstOrNull { it.bot.id == botId }
        bot?.getGroup(groupId)
      }
    }.filterNotNull().collect {
      try {
        it.backup()
      } catch (e: Exception) {
        logger.error(e) { "An error occurred when backup member" }
      }
    }
  }
}

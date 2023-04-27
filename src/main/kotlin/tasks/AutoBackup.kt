package me.hbj.bikkuri.tasks

import dev.inmo.krontab.doInfinity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import me.hbj.bikkuri.data.BackupTaskPersist
import me.hbj.bikkuri.utils.ModuleScope
import mu.KotlinLogging
import net.mamoe.mirai.Bot
import net.mamoe.mirai.utils.ConcurrentHashMap

val autoBackups = ConcurrentHashMap<Long, Job>()

private val logger = KotlinLogging.logger {}

@Volatile
var autoBackupModule: ModuleScope? = null

fun CoroutineScope.launchAutoBackupTask(): Job = launch {
  val module = ModuleScope("AutoBackup", this.coroutineContext, Dispatchers.IO)
  autoBackupModule = module
  BackupTaskPersist.backups.forEach { (groupId, backup) ->
    autoBackups[groupId] = module.launch {
      doInfinity(backup.cron) {
        val group = Bot.instances.asSequence().mapNotNull { it.getGroup(groupId) }.firstOrNull()
        if (group == null) {
          logger.warn { "无法获取群聊 $groupId 的实例, 机器人可能没有加入该群聊。" }
          return@doInfinity
        }
        group.backup()
      }
    }.apply {
      invokeOnCompletion {
        autoBackups.remove(groupId)
      }
    }
  }
}

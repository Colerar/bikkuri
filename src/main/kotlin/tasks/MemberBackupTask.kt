package me.hbj.bikkuri.tasks

import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.flow.asFlow
import me.hbj.bikkuri.configs.General
import me.hbj.bikkuri.utils.*
import mu.KotlinLogging
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.isAdministrator
import net.mamoe.mirai.contact.isOwner

private val logger = KotlinLogging.logger {}

class MemberBackupTask(
  val group: Group,
) {
  var totalMember: Int? = null
    private set

  var savedMember by atomic(0)
    private set

  suspend fun run() {
    logger.info { "Start backup for ${group.name}(${group.id})" }
    val file = resolveDataDirectory(
      "./member_backup/${group.id}/${now().toFriendly(General.data.timezone, Formatter.urlSafe)}.csv",
    ).apply {
      parentFile?.mkdirs()
      if (exists()) {
        logger.info { "File $absolutePath already exists, deleting..." }
        delete()
      }
    }
    val writer = csvWriter() { prependBOM = true }
    writer.openAsync(file) {
      writeRow("id", "name_card", "nick", "join_time", "last_msg", "is_admin", "is_owner")
      (group.members).apply {
        this@MemberBackupTask.totalMember = size
      }.asFlow().collect {
        try {
          logger.trace { "Write row $it" }
          writeRow(
            /* id */
            it.id,
            /* name_card */
            it.nameCard,
            /* nick */
            it.nick,
            /* join_time */
            it.joinTimestamp,
            /* last_msg */
            it.lastSpeakTimestamp,
            /* is_admin */
            it.isAdministrator(),
            /* is_owner */
            it.isOwner(),
          )
          this@MemberBackupTask.savedMember++
        } catch (e: Exception) {
          logger.error(e) { "Error occurred when backup member" }
        }
      }
    }
    logger.info { "Backup complete, file: ${file.absPath}" }
  }
}

suspend fun Group.backup() {
  sendMessage("⏱ 开始备份群员列表……")
  val task = MemberBackupTask(this).apply {
    run()
  }
  if (task.savedMember == 0) {
    sendMessage("❌ 备份时发生错误，详情请查看后台。")
  } else {
    sendMessage("✅ 备份完成! 已保存 ${task.savedMember} 名群员。")
  }
}

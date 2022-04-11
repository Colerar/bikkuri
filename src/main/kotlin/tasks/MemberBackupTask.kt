package me.hbj.bikkuri.tasks

import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.flow.asFlow
import me.hbj.bikkuri.Bikkuri
import me.hbj.bikkuri.util.now
import mu.KotlinLogging
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.isAdministrator
import net.mamoe.mirai.contact.isOwner

private val logger = KotlinLogging.logger { }

class MemberBackupTask(
  val group: Group,
) {
  var totalMember: Int? = null
    private set

  var savedMember by atomic(0)
    private set

  suspend fun run() {
    logger.info { "Start backup for ${group.name}(${group.id})" }
    val file = Bikkuri.resolveDataFile("./member_backup/${group.id}/${now().epochSeconds}.csv").apply {
      parentFile?.mkdirs()
      if (exists()) {
        logger.info { "File $absolutePath already exists, deleting..." }
        delete()
      }
      // write UTF-8 BOM to support Microsoft Office
      writeBytes(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
    }
    csvWriter().openAsync(file, append = true) {
      writeRow("id", "name_card", "nick", "join_time", "last_msg", "is_admin", "is_owner")
      (group.members).apply {
        this@MemberBackupTask.totalMember = size
      }.asFlow().collect {
        logger.trace { "Write row $it" }
        writeRow(
          /* id */ it.id,
          /* name_card */ it.nameCard,
          /* nick */ it.nick,
          /* join_time */ it.joinTimestamp,
          /* last_msg */ it.lastSpeakTimestamp,
          /* is_admin */ it.isAdministrator(),
          /* is_owner */ it.isOwner()
        )
        this@MemberBackupTask.savedMember++
      }
    }
  }
}

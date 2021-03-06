@file:UseSerializers(CronSerializer::class)

package me.hbj.bikkuri.data

import com.cronutils.model.Cron
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import me.hbj.bikkuri.util.nextExecutionTime
import me.hbj.bikkuri.util.now
import me.hbj.bikkuri.util.parseCron
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.contact.Group

object BackupTasks : AutoSavePluginData("BackupTasks") {
  private val set: MutableSet<BackupTask> by value(mutableSetOf())
  private val lock = Mutex()

  private suspend inline fun filterSet(bot: Bot, group: Group) = lock.withLock {
    set.filter { it.botId == bot.id }
      .firstOrNull { it.groupId == group.id }
  }

  suspend fun getAll() = lock.withLock {
    set.toList()
  }

  suspend fun add(backupTask: BackupTask) = lock.withLock {
    set.add(backupTask)
  }

  suspend fun contains(bot: Bot, group: Group) = filterSet(bot, group) != null

  suspend fun get(bot: Bot, group: Group) = filterSet(bot, group)

  suspend fun remove(task: BackupTask) = lock.withLock {
    set.remove(task)
  }

  suspend fun remove(bot: Bot, group: Group) = lock.withLock {
    set.removeIf { it.botId == bot.id && it.groupId == group.id }
  }
}

@Serializable
data class BackupTask(
  var cron: Cron,
  var redoWhenMissed: Boolean,
  var groupId: Long,
  var botId: Long,
) {
  // null for no last execution
  private var lastExecution: Instant? = null

  private val firstExec = nextExec(now())

  private fun nextExec(now: Instant) =
    cron.nextExecutionTime(now) ?: error("Failed to calculate next execution time")

  fun <T> withDo(now: Instant = now(), action: BackupTask.() -> T): T? {
    if (shouldDo(now)) {
      willDo()
      return action(this)
    }
    return null
  }

  /**
   * determine should or not run task
   */
  fun shouldDo(now: Instant = now()): Boolean {
    return lastExecution?.let { // lastExecution: not null
      now > nextExec(it) // if now > next execution
    } ?: (now > firstExec) // lastExecution: null, so if now > firstExec do job
  }

  /**
   * if a task triggered, should invoke it
   */
  fun willDo() {
    lastExecution = now()
  }
}

object CronSerializer : KSerializer<Cron> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("CronSerializer", PrimitiveKind.STRING)

  override fun deserialize(decoder: Decoder): Cron =
    parseCron(decoder.decodeString()) ?: error("Failed to deserialize Cron")

  override fun serialize(encoder: Encoder, value: Cron): Unit =
    encoder.encodeString(value.asString())
}

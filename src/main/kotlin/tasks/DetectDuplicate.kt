package me.hbj.bikkuri.tasks

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.hbj.bikkuri.db.DuplicateConfig
import me.hbj.bikkuri.db.DuplicateTable
import me.hbj.bikkuri.util.checkDuplicate
import me.hbj.bikkuri.util.toTreeString
import mu.KotlinLogging
import net.mamoe.mirai.Bot
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.toKotlinDuration

private val logger = KotlinLogging.logger {}

fun CoroutineScope.launchDetectDuplicate() = launch {
  while (isActive) {
    newSuspendedTransaction {
      val enabled = DuplicateConfig.find { DuplicateTable.enabled eq true }
      detectJobs.keys.toList().subtract(enabled.map { it.id.value }.toSet()).forEach {
        detectJobs[it]?.job?.cancel()
        detectJobs.remove(it)
      }
      enabled.forEach { config ->
        fun newJob() =
          DetectDuplicate(config.checkInterval, launch {
            newSuspendedTransaction {
              val bot = Bot.instances.first()
              config.groups.forEach {
                logger.info { "$it" }
              }
              val groups = config.groups.mapNotNull { bot.getGroup(it) }
              val toKick = bot.checkDuplicate(groups, config.allowed)
              if (toKick.size >= 50) error("Too many duplicate, may be program error...")
              // toKick.kickAll()
              if (toKick.isNotEmpty()) {
                groups.map { group ->
                  launch {
                    group.sendMessage("以下群员因重复进群被踢出：\n${toKick.toTreeString()}".trimEnd())
                  }
                }
              }
            }
            delay(config.checkInterval.toKotlinDuration())
          })

        val jobEntry = detectJobs.getOrPut(config.id.value, ::newJob)
        if (jobEntry.checkInterval != config.checkInterval) {
          jobEntry.job.cancel()
          detectJobs[config.id.value] = newJob()
        }
      }
    }
    delay(30_000)
  }
}

private val detectJobs = ConcurrentHashMap<Int, DetectDuplicate>()

data class DetectDuplicate(
  val checkInterval: Duration,
  val job: Job,
)

package me.hbj.bikkuri.util

import com.cronutils.model.Cron
import com.cronutils.model.CronType.UNIX
import com.cronutils.model.definition.CronDefinition
import com.cronutils.model.definition.CronDefinitionBuilder
import com.cronutils.model.time.ExecutionTime
import com.cronutils.parser.CronParser
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant

private val logger = mu.KotlinLogging.logger { }

private val cronDefinition: CronDefinition by lazy {
  CronDefinitionBuilder.instanceDefinitionFor(UNIX)
}

private val parser by lazy { CronParser(cronDefinition) }

fun parseCron(cron: String) = runCatching {
  parser.parse(cron)
}.onFailure {
  logger.warn(it) { "Failed to parse cron expression $cron" }
}.getOrNull()

fun Cron.nextExecutionTime(time: Instant = now()): Instant? {
  val executionTime = ExecutionTime.forCron(this)
  return executionTime.nextExecution(time.toZonedUtc()).unwrap()
    ?.toInstant()?.toKotlinInstant()
}

fun Cron.lastExecutionTime(time: Instant = now()): Instant? {
  val executionTime = ExecutionTime.forCron(this)
  return executionTime.lastExecution(time.toZonedUtc()).unwrap()
    ?.toInstant()?.toKotlinInstant()
}

fun Cron.serialize(): String = this.asString()

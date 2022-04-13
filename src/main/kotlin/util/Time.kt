package me.hbj.bikkuri.util

import kotlinx.atomicfu.atomic
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toJavaZoneId
import kotlinx.datetime.toLocalDateTime
import me.hbj.bikkuri.data.General
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Suppress("NOTHING_TO_INLINE")
inline fun now() = Clock.System.now()

fun afterNowDays(days: Int) = now() + days.toDuration(DurationUnit.DAYS)

val after1Day
  get() = afterNowDays(1)

val after7Days
  get() = afterNowDays(7)

val after30Days
  get() = afterNowDays(30)

object Formatter {
  val dateTime: DateTimeFormatter by atomic(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss"))
  val dateTime2: DateTimeFormatter by atomic(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"))
}

fun Instant.toFriendly(timeZone: TimeZone = General.timeZone, formatter: DateTimeFormatter = Formatter.dateTime): String? =
  formatter.format(toLocalDateTime(timeZone).toJavaLocalDateTime())

fun Instant.toZonedUtc(): ZonedDateTime = toJavaInstant().atZone(TimeZone.UTC.toJavaZoneId())

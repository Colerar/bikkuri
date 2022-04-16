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
import kotlin.time.Duration
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

fun Instant.toFriendly(
  timeZone: TimeZone = General.timeZone,
  formatter: DateTimeFormatter = Formatter.dateTime
): String? =
  formatter.format(toLocalDateTime(timeZone).toJavaLocalDateTime())

fun Instant.toZonedUtc(): ZonedDateTime = toJavaInstant().atZone(TimeZone.UTC.toJavaZoneId())

private val durationRegex =
  /* ktlint-disable max-line-length */
  Regex("""((\d{1,2})[yY年])?((\d{1,2})(月|mon|Mon))?((\d{1,2})[dD天])?((\d{1,2})(小时|时|h|H))?((\d{1,2})(分钟|分|m|M)(?!on))?((\d{1,7})(秒钟|秒|s|S))?""")
/* ktlint-enable max-line-length */

private val yearRegex = Regex("""(\d{1,2})[yY年]""")
private val monRegex = Regex("""(\d{1,2})(月|mon|Mon)""")
private val dayRegex = Regex("""(\d{1,2})[dD天]""")
private val hourRegex = Regex("""(\d{1,2})(小时|时|h|H)""")
private val minRegex = Regex("""(\d{1,2})(分钟|分|m|M)(?!on)""")
private val secRegex = Regex("""(\d{1,7})(秒钟|秒|s|S)""")
private val numberRegex = Regex("""^\d+$""")
fun parseTime(expr: String): Duration? {
  if (numberRegex.matches(expr)) {
    return expr.toLongOrNull()?.toDuration(DurationUnit.SECONDS)
  }

  if (durationRegex.matches(expr)) {
    var totalTime: Long = 0
    totalTime += yearRegex.find(expr)?.groups?.get(1)?.value?.toLong()?.times(365 * 24 * 60 * 60) ?: 0L
    totalTime += monRegex.find(expr)?.groups?.get(1)?.value?.toLong()?.times(30 * 24 * 60 * 60) ?: 0L
    totalTime += dayRegex.find(expr)?.groups?.get(1)?.value?.toLong()?.times(24 * 60 * 60) ?: 0L
    totalTime += hourRegex.find(expr)?.groups?.get(1)?.value?.toLong()?.times(60 * 60) ?: 0L
    totalTime += minRegex.find(expr)?.groups?.get(1)?.value?.toLong()?.times(60) ?: 0L
    totalTime += secRegex.find(expr)?.groups?.get(1)?.value?.toLong() ?: 0L

    return totalTime.toDuration(DurationUnit.SECONDS)
  }

  return null
}

fun Duration.toFriendly(msMode: Boolean = false): String {
  toComponents { days, hours, minutes, seconds, ns ->
    return buildString {
      if (days != 0L) append("${days}天")
      if (hours != 0) append("${hours}时")
      if (minutes != 0) append("${minutes}分")
      if (seconds != 0) append("${seconds}秒")
      if (msMode) append("${ns / 1_000_000}毫秒")
    }
  }
}

fun Duration.toHMS() = toComponents { h, m, s, _ ->
  val hour = h.toString().padStart(2, '0')
  val min = m.toString().padStart(2, '0')
  val sec = s.toString().padStart(2, '0')
  "$hour:$min:$sec"
}

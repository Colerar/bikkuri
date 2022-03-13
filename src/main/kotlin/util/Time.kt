package me.hbj.bikkuri.util

import kotlinx.datetime.Clock
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

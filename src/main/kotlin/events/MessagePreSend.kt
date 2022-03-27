package me.hbj.bikkuri.events

import kotlinx.coroutines.delay
import me.hbj.bikkuri.data.General
import net.mamoe.mirai.event.events.MessagePreSendEvent

fun Events.onMessagePreSend() {
  subscribeAlways<MessagePreSendEvent> {
    val (start, end) = General.time.replayDelayRange
    if (start == 0L && end == 0L) return@subscribeAlways
    delay((start..end).random())
  }
}

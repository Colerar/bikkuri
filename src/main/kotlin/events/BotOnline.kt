package me.hbj.bikkuri.events

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import net.mamoe.mirai.event.events.BotOnlineEvent
import net.mamoe.mirai.utils.ConcurrentHashMap

val botLastOnline = ConcurrentHashMap<Long, Instant>()

fun Events.onBotOnline() {
  subscribeAlways<BotOnlineEvent> {
    botLastOnline[it.bot.id] = Clock.System.now()
  }
}

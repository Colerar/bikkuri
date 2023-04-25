package me.hbj.bikkuri.events

import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import net.mamoe.mirai.event.events.BotOfflineEvent
import net.mamoe.mirai.event.events.BotOnlineEvent
import net.mamoe.mirai.event.nextEvent

fun Events.onBotOffline() {
  subscribeAlways<BotOfflineEvent> { event ->
    event.bot.launch {
      val online = withTimeoutOrNull(20_000) {
        nextEvent<BotOnlineEvent>() { it.bot == bot }
      }
      if (online != null) return@launch
      commandCtxManager.ctxMap
        .asSequence()
        .filter { (id, _) -> id.bot == bot.id }
        .map { (_, job) -> job }
        .toList()
        .forEach { it.cancel() }
    }
  }
}

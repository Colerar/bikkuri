package me.hbj.bikkuri.events

import kotlinx.datetime.Clock
import me.hbj.bikkuri.data.LastMsg
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.EventChannel
import net.mamoe.mirai.event.events.BotOnlineEvent

fun EventChannel<Event>.onBotOnline() {
  val now = Clock.System.now()
  subscribeAlways<BotOnlineEvent> {
    LastMsg.map.forEach { (_, group) ->
      group.members.forEach { (k, _) ->
        group.members[k] = now
      }
    }
  }
}

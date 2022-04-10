package me.hbj.bikkuri.events

import me.hbj.bikkuri.data.GlobalLastMsg
import net.mamoe.mirai.event.events.BotOnlineEvent

fun Events.onBotOnline() {
  subscribeAlways<BotOnlineEvent> {
    GlobalLastMsg.remove(bot.id)
  }
}

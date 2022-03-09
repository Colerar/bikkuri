package me.hbj.bikkuri.events

import me.hbj.bikkuri.data.ListenerData
import net.mamoe.mirai.contact.getMember
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.EventChannel
import net.mamoe.mirai.event.events.BotOfflineEvent

fun EventChannel<Event>.onBotOffline() {
  subscribeAlways<BotOfflineEvent.Active> {
    val enabledMap = ListenerData.enabledMap
    queuedMemberRequest.asSequence()
      .filter { it.bot.id == bot.id }
      .filter { enabledMap.keys.contains(it.groupId) }
      .filter { it.group?.getMember(this.bot.id)?.isOperator() == false }
      .forEach {
        it.accept()
        queuedMemberRequest.remove(it)
      }
  }
}

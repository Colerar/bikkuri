package me.hbj.bikkuri.tasks

import kotlinx.coroutines.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.hbj.bikkuri.data.ListenerPersist
import me.hbj.bikkuri.data.TimerTrigger.ON_JOIN
import me.hbj.bikkuri.data.TimerTrigger.ON_MSG
import me.hbj.bikkuri.events.botLastOnline
import me.hbj.bikkuri.utils.now
import me.hbj.bikkuri.utils.sendMessage
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.message.data.At
import kotlin.time.DurationUnit

fun CoroutineScope.launchAutoKickTask(): Job = launch {
  while (isActive) {
    val now = Clock.System.now()
    delay(5_000)
    Bot.instances.forEach bot@{ bot ->
      ListenerPersist.listeners.filterValues { it.enable }.forEach group@{ (groupId, data) ->
        val group = bot.getGroup(groupId) ?: return@group
        if (data.kickDuration <= 0) return@group
        group.members.asSequence()
          .filter { !it.isOperator() }
          .filter {
            val botLastOnline = botLastOnline.computeIfAbsent(bot.id) { now() }
            val timestamp = when (data.trigger) {
              ON_JOIN -> it.joinTimestamp
              ON_MSG -> if (it.lastSpeakTimestamp == 0) it.joinTimestamp else it.lastSpeakTimestamp
            }.toLong()
            val memberLastActivity = Instant.fromEpochSeconds(timestamp)
            val isTimeout = (now - memberLastActivity).toLong(DurationUnit.SECONDS) > data.kickDuration
            isTimeout && memberLastActivity > botLastOnline
          }.forEach {
            val message = "您已 ${data.kickDuration} 秒内无回复，已将您移出群聊，请重新排队申请加群。"
            group.sendMessage {
              +At(it)
              +" "
              +message
            }
            delay(5_000L)
            it.kick(message)
          }
      }
    }
  }
}

package me.hbj.bikkuri.tasks

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import me.hbj.bikkuri.data.General
import me.hbj.bikkuri.data.GlobalLastMsg
import me.hbj.bikkuri.data.ListenerData
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.isOperator
import kotlin.time.DurationUnit
import kotlin.time.toDuration

fun CoroutineScope.launchAutoKickTask(): Job = launch {
  while (isActive) {
    val now = Clock.System.now()
    delay(General.time.autoKick)
    ListenerData.map.forEach { (t, _) ->
      Bot.instances.forEach { bot ->
        bot.groups.asSequence().filter {
          it.id == t && it.botAsMember.isOperator()
        }.forEach group@{ group ->
          group.members
            .asSequence()
            .filterNot { it.isOperator() }
            .forEach member@{
              val sec = ListenerData.map[group.id]?.kickDuration?.toLong() ?: 0
              if (sec == 0L) return@member
              val duration = sec.toDuration(DurationUnit.SECONDS)
              val map = GlobalLastMsg[bot.id][group.id].map
              val instant = map[it.id] ?: return@group
              if (now - instant > duration) {
                val message = "您已 ${duration.inWholeSeconds} 秒无回复，已将你移出群聊，请重新排队申请加群。"
                it.sendMessage(message)
                delay(General.time.messageNoticeBetweenKick)
                it.kick(message)
                map.remove(it.id)
              }
            }
        }
      }
    }
  }
}

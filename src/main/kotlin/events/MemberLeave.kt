package me.hbj.bikkuri.events

import me.hbj.bikkuri.command.JobIdentity
import net.mamoe.mirai.event.events.MemberLeaveEvent

fun Events.onMemberLeave() {
  subscribeAlways<MemberLeaveEvent> { event ->
    commandCtxManager.cancelJob(JobIdentity(event.bot.id, event.groupId, event.member.id))
  }
}

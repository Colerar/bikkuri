package me.hbj.bikkuri.events

import me.hbj.bikkuri.data.GlobalAutoApprove
import me.hbj.bikkuri.data.ListenerData
import me.hbj.bikkuri.db.isBlocked
import mu.KotlinLogging
import net.mamoe.mirai.event.events.MemberJoinRequestEvent

private val logger = KotlinLogging.logger {}

fun Events.onMemberRequest() {
  subscribeAlways<MemberJoinRequestEvent> { event ->
    // 自动拒绝黑名单用户，优先级最高
    if (group?.isBlocked(fromId) == true) {
      event.reject(message = "你已被屏蔽。")
      return@subscribeAlways
    }
    // 为舰长群自动审核
    val group = GlobalAutoApprove[bot.id][groupId].map
    if (group.containsKey(fromId)) {
      event.accept()
      group.remove(fromId)
      logger.info { "Accepted MemberJoinRequestEvent because 'In auto approve list' for $this" }
    }
    // 将审核群的申请添加到审核列表
    run {
      if (!ListenerData.map.keys.contains(event.groupId)) return@run
      queuedMemberRequest.add(this)
      logger.info { "Add MemberJoinRequestEvent to queue: $this" }
    }
  }
}

val queuedMemberRequest = mutableListOf<MemberJoinRequestEvent>()

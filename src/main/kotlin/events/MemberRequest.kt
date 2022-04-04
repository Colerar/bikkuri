package me.hbj.bikkuri.events

import me.hbj.bikkuri.data.AutoApprove
import me.hbj.bikkuri.data.AutoApproveData
import me.hbj.bikkuri.data.ListenerData
import me.hbj.bikkuri.db.isBlocked
import mu.KotlinLogging
import net.mamoe.mirai.event.events.MemberJoinRequestEvent

private val logger = KotlinLogging.logger {}

fun Events.onMemberRequest() {
  subscribeAlways<MemberJoinRequestEvent> {
    // 自动拒绝黑名单用户，优先级最高
    if (group?.isBlocked(fromId) == true) {
      it.reject(message = "你已被屏蔽。")
      return@subscribeAlways
    }
    // 为舰长群自动审核
    val list = AutoApprove.map.getOrPut(groupId) { AutoApproveData() }.set
    if (list.contains(fromId)) {
      it.accept()
      AutoApprove.map[groupId]?.set?.remove(fromId)
      logger.info { "Accepted MemberJoinRequestEvent because 'In auto approve list' for $this" }
    }
    // 将审核群的申请添加到审核列表
    run {
      if (!ListenerData.map.keys.contains(it.groupId)) return@run
      queuedMemberRequest.add(this)
      logger.info { "Add MemberJoinRequestEvent to queue: $this" }
    }
  }
}

val queuedMemberRequest = mutableListOf<MemberJoinRequestEvent>()

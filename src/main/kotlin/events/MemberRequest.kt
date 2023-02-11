package me.hbj.bikkuri.events

import kotlinx.datetime.toJavaInstant
import me.hbj.bikkuri.data.GlobalAutoApprove
import me.hbj.bikkuri.data.ListenerData
import me.hbj.bikkuri.db.BotAccepted
import me.hbj.bikkuri.db.isBlocked
import me.hbj.bikkuri.util.now
import mu.KotlinLogging
import net.mamoe.mirai.event.events.MemberJoinRequestEvent
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.ConcurrentLinkedDeque

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
      group.remove(fromId)?.also { removed ->
        transaction {
          BotAccepted.insert {
            it[instant] = now().toJavaInstant()
            it[botId] = bot.id
            it[fromId] = event.fromId
            it[boundBiliId] = removed.boundBiliUid
            it[fromGroupId] = removed.fromGroup
            it[toGroupId] = event.groupId
          }
        }
        logger.info { "Write auto approve operation to db: $fromId - $removed" }
      }
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

val queuedMemberRequest = ConcurrentLinkedDeque<MemberJoinRequestEvent>()

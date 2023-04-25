package me.hbj.bikkuri.events

import kotlinx.datetime.toJavaInstant
import me.hbj.bikkuri.data.ListenerPersist
import me.hbj.bikkuri.db.BotAccepted
import me.hbj.bikkuri.db.isBlocked
import me.hbj.bikkuri.utils.now
import mu.KotlinLogging
import net.mamoe.mirai.event.events.MemberJoinRequestEvent
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.ConcurrentHashMap
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
    val groups = globalAutoApprove.computeIfAbsent(this.bot.id) { ConcurrentHashMap() }
    val deque = groups[event.group?.id]
    val approved = deque?.firstOrNull { it.memberId == fromId }
    if (approved != null) {
      event.accept()
      deque.remove(approved)
      transaction {
        BotAccepted.insert {
          it[instant] = now().toJavaInstant()
          it[botId] = bot.id
          it[fromId] = event.fromId
          it[boundBiliId] = approved.userBiliUid
          it[fromGroupId] = approved.fromGroup
          it[toGroupId] = event.groupId
        }
        logger.info { "Write auto approve operation to db: $fromId - $approved" }
      }
      logger.info { "Auto Accepted MemberJoinRequestEvent for $this" }
    }

    // 将审核群的申请添加到审核列表
    run {
      if (!ListenerPersist.listeners.containsKey(event.groupId)) return@run
      queuedMemberRequest.add(this)
      logger.info { "Add MemberJoinRequestEvent to queue: $this" }
    }
  }
}

val queuedMemberRequest = ConcurrentLinkedDeque<MemberJoinRequestEvent>()

// bot id to (target group to member infos)
val globalAutoApprove = ConcurrentHashMap<
  Long,
  ConcurrentHashMap<
    Long,
    ConcurrentLinkedDeque<AutoApprove>,
    >,
  >()

data class AutoApprove(
  val memberId: Long,
  val fromGroup: Long,
  val userBiliUid: Long,
)

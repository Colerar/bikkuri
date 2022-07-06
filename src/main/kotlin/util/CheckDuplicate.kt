package me.hbj.bikkuri.util

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import me.hbj.bikkuri.db.BotAccepted
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.contact.isOperator
import org.jetbrains.exposed.sql.select

suspend fun Bot.checkDuplicate(groups: Collection<Group>, allowList: Collection<Long>): List<NormalMember> {
  val members = groups
    .mapNotNull { bot.getGroup(it.id) }
    .flatMap { it.members }
  val asyncMap = members.associate { member ->
    member.id to coroutineScope {
      async {
        BotAccepted.select { BotAccepted.eqMember(member) }.map { it[BotAccepted.boundBiliId] }
      }
    }
  }
  return members
    .groupBy { member ->
      Identity(member.id, asyncMap[member.id]?.await().orEmpty())
    }.values
    .filter { it.size >= 2 }
    .filterNot { list -> list.any { it.isOperator() } }
    .flatMap { it.shuffled().take(it.size - 1) }
    .filterNot { allowList.contains(it.id) }
    .toList()
}

data class Identity(
  val qq: Long,
  val buid: List<Long>,
) {
  // 只要 QQ 或 B站 UID 任何一个有关联 都判定为相等
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Identity) return false
    if (qq == other.qq) return true
    if (buid.any { i -> other.buid.any { j -> i == j } }) return true
    return false
  }

  override fun hashCode(): Int = 0
}

suspend fun Collection<NormalMember>.kickAll(limit: Int = 4) {
  val limiter = Semaphore(limit)
  val scope = ModuleScope("MemberDuplicateKicker")
  map {
    scope.launch {
      limiter.withPermit {
        it.kick("重复加群")
      }
    }
  }.joinAll()
}

fun Collection<NormalMember>.toTreeString() =
  buildString {
    this@toTreeString.groupBy { it.group }.forEach { (group, member) ->
      appendLine(group.name)
      member.forEach {
        append("  - ")
        appendLine(it.toFriendly())
      }
    }
  }

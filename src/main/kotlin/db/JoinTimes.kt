package me.hbj.bikkuri.db

import net.mamoe.mirai.contact.Member
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

private val logger = mu.KotlinLogging.logger {}

object JoinTimes : Table() {
  val groupId = long("group_id").index()
  val memberId = long("member_id").index()
  val joinTimes = uinteger("join_times")

  fun query(groupId: Long, memberId: Long) = transaction {
    val db = this@JoinTimes
    select {
      (db.groupId eq groupId) and (db.memberId eq memberId)
    }.firstOrNull()?.let { it[joinTimes] }
  }

  fun contains(groupId: Long, memberId: Long) = query(groupId, memberId) != null

  fun insert(groupId: Long, memberId: Long) = transaction {
    insert {
      it[this.groupId] = groupId
      it[this.memberId] = memberId
      it[joinTimes] = 1u
    }
    1u
  }

  fun update(groupId: Long, memberId: Long, times: UInt) = transaction {
    val db = this@JoinTimes
    update(
      where = { (db.groupId eq groupId) and (db.memberId eq memberId) },
      body = { it[joinTimes] = times },
    )
    times
  }

  fun delete(groupId: Long, memberId: Long) = transaction {
    val db = this@JoinTimes
    deleteWhere { (db.groupId eq groupId) and (db.memberId eq memberId) }
  }
}

/**
 * record member join time,
 * if no record, init to 1, else increment
 * @return total join times after this
 */
fun Member.recordJoin(): UInt {
  val db = JoinTimes
  val group = group.id
  val member = id
  logger.info { "" }
  val before = db.query(group, member)
  val now = before?.let {
    val now = before + 1u
    db.update(group, member, times = now)
  } ?: db.insert(group, member)
  logger.info { "Member $member joined Group $group, joined $now times" }
  return now
}

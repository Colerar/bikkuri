package me.hbj.bikkuri.db

import kotlinx.datetime.Clock.System.now
import kotlinx.datetime.toJavaInstant
import me.hbj.bikkuri.db.Blocklist.eqToGroup
import me.hbj.bikkuri.db.Blocklist.memberId
import me.hbj.bikkuri.db.Blocklist.updateInstant
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.transactions.transaction

object Blocklist : Table() {
  private val eventId = long("event_id").autoIncrement()
  val botId = long("bot_id")
  val groupId = long("group_id").index()
  val memberId = long("member_id")
  val updateInstant = timestamp("updateInstant")
  override val primaryKey = PrimaryKey(eventId)

  fun eqToGroup(group: Group) = with(group) {
    (groupId eq group.redirectBlockId())
  }

  fun eqToMember(member: Member) = with(member) {
    (groupId eq group.redirectBlockId()) and
      (memberId eq id)
  }
}

fun Group.listBlocked(page: Long, size: Int = 10) = transaction {
  Blocklist
    .select { eqToGroup(this@listBlocked) }
    .orderBy(updateInstant, SortOrder.DESC)
    .limit(size, (page - 1) * size)
    .map { it[memberId] to it[updateInstant] }
}

fun Group.blockedTime(id: Long) = transaction {
  Blocklist
    .select { eqToGroup(this@blockedTime) and (memberId eq id) }
    .map { it[updateInstant] }
    .firstOrNull()
}

fun Member.blockedTime(id: Long) = transaction {
  group.blockedTime(id)
}

fun Group.blockedSize() = transaction {
  Blocklist
    .select { eqToGroup(this@blockedSize) }
    .count()
}

fun Group.addBlock(id: Long) = transaction {
  Blocklist.insert {
    it[botId] = bot.id
    it[groupId] = this@addBlock.redirectBlockId()
    it[memberId] = id
    it[updateInstant] = now().toJavaInstant()
  }
}

fun Group.removeBlock(id: Long) = transaction {
  Blocklist.deleteWhere {
    eqToGroup(this@removeBlock) and (memberId eq id)
  }
}

fun Group.isBlocked(id: Long) = transaction {
  Blocklist.select {
    eqToGroup(this@isBlocked) and (memberId eq id)
  }.count() >= 1
}

fun Member.addBlock() = group.addBlock(id)

fun Member.isBlocked() = group.isBlocked(id)

fun Member.removeBlock() = group.removeBlock(id)

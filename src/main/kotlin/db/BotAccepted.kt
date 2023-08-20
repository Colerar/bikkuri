package me.hbj.bikkuri.db

import net.mamoe.mirai.contact.Member
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.or

object BotAccepted : Table() {
  private val eventId = long("event_id").autoIncrement()

  val instant = timestamp("instant")

  val botId = long("bot_id").index()

  // 被同意的 Q 号请求
  val fromId = long("from_id").index()

  // 与之绑定的 B 站账户 UID
  val boundBiliId = long("bound_bili_id").index()

  // 从哪个审核群进入的
  val fromGroupId = long("from_group_id").index()

  // 去往哪个群
  val toGroupId = long("to_group_id").index()

  override val primaryKey: PrimaryKey = PrimaryKey(eventId)

  fun eq(bot: Long, group: Long): Op<Boolean> =
    ((fromGroupId eq group) or (toGroupId eq group))

  fun eqMember(memberId: Long): Op<Boolean> = fromId eq memberId
  fun eqMember(member: Member): Op<Boolean> = fromId eq member.id

  fun eqBiliUser(id: Long): Op<Boolean> = boundBiliId eq id
}

package me.hbj.bikkuri.db

import me.hbj.bikkuri.db.BiliBlocklist.eqToGroup
import net.mamoe.mirai.contact.Group
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object BiliBlocklist : Table() {
  val botId = long("bot_id")
  val groupId = long("group_id").index()
  val uid = long("bili_uid")

  fun eqToGroup(group: Group) = with(group) {
    (botId eq bot.id) and
      (groupId eq group.redirectBlockId())
  }
}

fun Group.addBiliBlock(uid: Long) = transaction {
  BiliBlocklist.insert {
    it[this.uid] = uid
    it[groupId] = redirectBlockId()
    it[botId] = bot.id
  }
}

fun Group.isBiliBlocked(uid: Long) = transaction {
  BiliBlocklist.select {
    eqToGroup(this@isBiliBlocked) and
      (BiliBlocklist.uid eq uid)
  }.count() > 0
}

fun Group.removeBiliBlock(uid: Long) = transaction {
  BiliBlocklist.deleteWhere {
    eqToGroup(this@removeBiliBlock) and
      (BiliBlocklist.uid eq uid)
  }
}

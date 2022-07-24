package me.hbj.bikkuri.db

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object BlocklistLink : Table() {
  private val fromGroupId = long("from_group_id")
  private val toGroupId = long("to_group_id")

  override val primaryKey: PrimaryKey = PrimaryKey(fromGroupId)

  fun related(bot: Bot, group: Group) = transaction {
    val opId = group.redirectBlockId()
    buildList {
      add(group)
      select {
        toGroupId eq opId
      }.mapNotNull {
        bot.getGroup(it[fromGroupId])
      }.also {
        addAll(it)
      }
    }
  }

  fun link(from: Long, to: Long): Unit = transaction {
    insert {
      it[fromGroupId] = from
      it[toGroupId] = to
    }
  }

  fun remove(from: Long) = transaction {
    deleteWhere {
      fromGroupId eq from
    }
  }

  fun update(from: Long, newTo: Long) = transaction {
    update(
      where = {
        fromGroupId eq from
      },
      body = {
        it[toGroupId] = newTo
      },
    )
  }

  fun query(from: Long) = transaction {
    select {
      fromGroupId eq from
    }.firstOrNull()?.let {
      it[toGroupId]
    }
  }

  fun contains(from: Long) = query(from) != null
}

fun Group.redirectBlockId() = BlocklistLink.query(this.id) ?: id

package me.hbj.bikkuri.db

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import me.hbj.bikkuri.data.GuardFetcher
import me.hbj.bikkuri.util.now
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object GuardList : Table() {
  private val liverId = long("liver_id")
  private val biliUserId = long("bili_user_id")
  private val expiresAt = timestamp("expires_at")
  private val fetcherType = enumeration("fetcher_type", GuardFetcher::class)

  override val primaryKey = PrimaryKey(liverId, biliUserId)

  fun cleanup() = transaction {
    val now = now().toJavaInstant()
    deleteWhere {
      expiresAt less now
    }
  }

  fun count() = transaction {
    selectAll().count()
  }

  fun validate(liverMid: Long, biliUserUid: Long, instant: Instant = now()) = transaction {
    select {
      (liverId eq liverMid) and
        (biliUserId eq biliUserUid) and
        (expiresAt greaterEq instant.toJavaInstant())
    }.count() >= 1
  }

  fun contains(liverMid: Long, guardUid: Long) = transaction {
    select {
      (liverId eq liverMid) and (biliUserId eq guardUid)
    }.firstOrNull() != null
  }

  fun insertOrUpdate(liverUid: Long, guardUid: Long, time: Instant, fetcher: GuardFetcher): Unit = transaction {
    if (contains(liverUid, guardUid)) {
      update(liverUid, guardUid, time, fetcher)
    } else {
      insert(liverUid, guardUid, time, fetcher)
    }
  }

  fun insert(liverUid: Long, guardUid: Long, time: Instant, fetcher: GuardFetcher) = transaction {
    insert {
      it[liverId] = liverUid
      it[biliUserId] = guardUid
      it[expiresAt] = time.toJavaInstant()
      it[fetcherType] = fetcher
    }
  }

  fun update(liverUid: Long, guardUid: Long, time: Instant, fetcher: GuardFetcher) = transaction {
    update(
      where = {
        (liverId eq liverUid) and (biliUserId eq guardUid)
      },
      body = {
        it[expiresAt] = time.toJavaInstant()
        it[fetcherType] = fetcher
      }
    )
  }
}

/**
 * 用于存储上一次 LIST 的的更新时间
 */
object GuardLastUpdate : Table() {
  val liverId = long("liver_id")
  val lastUpdate = timestamp("last_update")

  fun insertOrUpdate(liverMid: Long, instant: Instant) {
    if (contains(liverMid)) {
      update(liverMid, instant)
    } else {
      insert(liverMid, instant)
    }
  }

  fun get(liverMid: Long) = transaction {
    select { liverId eq liverMid }.firstOrNull()
  }

  fun contains(liverMid: Long) = get(liverMid) != null

  fun insert(liverMid: Long, instant: Instant): Unit = transaction {
    insert {
      it[liverId] = liverMid
      it[lastUpdate] = instant.toJavaInstant()
    }
  }

  fun update(liverMid: Long, instant: Instant) = transaction {
    update(
      where = { liverId eq liverMid },
      body = { it[lastUpdate] = instant.toJavaInstant() }
    )
  }
}

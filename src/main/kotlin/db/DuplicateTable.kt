package me.hbj.bikkuri.db

import me.hbj.bikkuri.db.sql.SQLDatabaseSet
import me.hbj.bikkuri.db.sql.SetTable
import me.hbj.bikkuri.utils.toFriendly
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.javatime.duration
import org.jetbrains.exposed.sql.select
import kotlin.time.DurationUnit.MINUTES
import kotlin.time.toDuration
import kotlin.time.toJavaDuration
import kotlin.time.toKotlinDuration

object DuplicateTable : IdTable<Int>("duplicate_table") {
  override val id = integer("id").autoIncrement().entityId()
  val enabled = bool("enabled").default(false)
  val checkInterval = duration("check_interval")
    .default(10.toDuration(MINUTES).toJavaDuration())
  override val primaryKey: PrimaryKey = PrimaryKey(id)
}

class DuplicateConfig(id: EntityID<Int>) : IntEntity(id) {
  companion object : IntEntityClass<DuplicateConfig>(DuplicateTable)

  var enabled by DuplicateTable.enabled
  var checkInterval by DuplicateTable.checkInterval
  val groups = SQLDatabaseSet(id, DuplicateGroupSet)
  val allowed = SQLDatabaseSet(id, DuplicateAllowlist)

  fun toFriendly(longMessage: Boolean = false) = if (longMessage) {
    """
    [${id.value}] ${if (enabled) "✅ 已开启" else "🚫 未开启"}
    相关群聊: ${groups.joinToString(", ")}
    检测间隔: ${checkInterval.toKotlinDuration().toFriendly()}
    """.trimIndent()
  } else {
    buildString {
      append("[${id.value}] ")
      append(if (enabled) "✅ 已开启" else "🚫 未开启")
      append("，相关群聊: ")
      if (groups.isEmpty()) {
        append("无")
      } else {
        append(groups.joinToString(", "))
      }
    }
  }
}

object DuplicateGroupSet : SetTable<Int, Long>("duplicate_group_set") {
  override val id =
    reference("id", DuplicateTable, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)

  override val value: Column<Long> = long("group_id")

  override val primaryKey: PrimaryKey = PrimaryKey(id, value)
}

object DuplicateAllowlist : SetTable<Int, Long>("duplicate_allowlist") {
  override val id =
    reference("id", DuplicateTable, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)

  override val value: Column<Long> = long("allowed_member")

  override val primaryKey: PrimaryKey = PrimaryKey(id, value)

  fun hasMember(id: Int, value: Long) =
    select {
      (this@DuplicateAllowlist.id eq id) and
        (this@DuplicateAllowlist.value eq value)
    }.count() >= 1
}

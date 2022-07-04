package me.hbj.bikkuri.db

import me.hbj.bikkuri.db.ForwardTable.enabled
import me.hbj.bikkuri.db.ForwardTable.forwardAll
import me.hbj.bikkuri.db.ForwardTable.id
import me.hbj.bikkuri.db.sql.SQLDatabaseSet
import me.hbj.bikkuri.db.sql.SetTable
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption.CASCADE

/**
 * @property id 被转发的群
 * @property enabled 开启转发
 * @property forwardAll 转发所有人, `true` 时转发任何人,
 * `false` 时仅转发位于名单中的
 */
object ForwardTable : IdTable<Long>("forward_table") {
  override val id: Column<EntityID<Long>> = long("group").entityId()
  val enabled = bool("enabled").default(false)
  val forwardAll = bool("forward_all").default(false)
}

class ForwardRelation(id: EntityID<Long>) : LongEntity(id) {
  companion object : LongEntityClass<ForwardRelation>(ForwardTable) {
    @Suppress("NOTHING_TO_INLINE")
    inline fun findByIdOrNew(id: Long, noinline init: ForwardRelation.() -> Unit = {}) =
      ForwardRelation.findById(id) ?: ForwardRelation.new(id) { init() }
  }

  var enabled by ForwardTable.enabled
  var forwardAll by ForwardTable.forwardAll
  val toGroups = SQLDatabaseSet(id, ForwardToGroupSet)
  val forwardees = SQLDatabaseSet(id, ForwardeeSet)
}

object ForwardToGroupSet : SetTable<Long, Long>("forward_to_group_set") {
  override val id =
    reference("from_group", ForwardTable, onDelete = CASCADE, onUpdate = CASCADE)

  override val value: Column<Long> = long("to_group")

  override val primaryKey: PrimaryKey = PrimaryKey(id, value)
}

object ForwardeeSet : SetTable<Long, Long>("forwardee_set") {
  override val id =
    reference("from_group", ForwardTable, onDelete = CASCADE, onUpdate = CASCADE)

  override val value: Column<Long> = long("to_group")

  override val primaryKey: PrimaryKey = PrimaryKey(id, value)
}

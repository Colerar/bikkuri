package me.hbj.bikkuri.db

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column

object DupAllow : IdTable<Long>("dup_allow_table") {
  override val id: Column<EntityID<Long>> = long("qq_id").entityId()
}

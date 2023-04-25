package me.hbj.bikkuri.db.sql

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.select

abstract class CacheableData<TID : Comparable<TID>>(
  protected val id: EntityID<TID>,
  private val table: IdTable<TID>,
) {

  private var cache: Query = originalData
  private var needToUpdate = true

  protected val originalData: Query
    get() = run {
      if (needToUpdate) {
        needToUpdate = false
        cache = table.select { table.id eq id }
      }
      return cache
    }

  protected fun needToUpdate() {
    needToUpdate = true
  }
}

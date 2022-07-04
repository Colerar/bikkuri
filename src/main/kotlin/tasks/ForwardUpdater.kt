package me.hbj.bikkuri.tasks

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.hbj.bikkuri.db.ForwardRelation
import me.hbj.bikkuri.db.ForwardTable
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.ConcurrentHashMap

val groupsToForward = ConcurrentHashMap<Long, ForwardRelation>()

fun CoroutineScope.launchUpdateForwardTask() = launch {
  while (isActive) {
    coroutineScope {
      transaction {
        groupsToForward.clear()
        groupsToForward.putAll(
          ForwardTable.selectAll().map { it[ForwardTable.id] }.map {
            it.value to ForwardRelation.findById(it)!!
          }.toMap()
        )
      }
      delay(60_000)
    }
  }
}

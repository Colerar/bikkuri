package me.hbj.bikkuri.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import me.hbj.bikkuri.persist.DataFilePersist
import me.hbj.bikkuri.utils.prettyJson
import me.hbj.bikkuri.utils.resolveDataDirectory
import java.util.concurrent.ConcurrentHashMap

object BackupTaskPersist : DataFilePersist<Map<Long, Backup>>(
  resolveDataDirectory("backup-task.json"),
  ConcurrentHashMap<Long, Backup>(),
  MapSerializer(Long.serializer(), Backup.serializer()),
  prettyJson,
) {
  val backups
    get() = run {
      if (data !is ConcurrentHashMap<*, *>) {
        data = ConcurrentHashMap(data)
      }
      data as ConcurrentHashMap<Long, Backup>
    }
}

@Serializable
data class Backup(
  val cron: String,
)

package me.hbj.bikkuri.data

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import me.hbj.bikkuri.util.randomKeygen
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value
import kotlin.time.Duration

object Keygen : AutoSavePluginData("KeygenList") {
  // QQ ID to keygen info
  val map: MutableMap<Long, KeygenData> by value()

  fun cleanup() {
    val now = Clock.System.now()
    map.asSequence()
      .filter { it.value.expiresAt < now }
      .forEach { (t, _) -> map.remove(t) }
  }
}

@Serializable
data class KeygenData(
  val salt: String,
  val keygen: String,
  val expiresAt: Instant,
)

fun KeygenData(salt: String, length: Int, expiresAfter: Duration): KeygenData =
  KeygenData(
    salt = salt,
    keygen = randomKeygen(length),
    expiresAt = Clock.System.now().plus(expiresAfter),
  )

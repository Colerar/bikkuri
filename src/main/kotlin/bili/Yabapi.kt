package me.hbj.bikkuri.bili

import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import me.hbj.bikkuri.bili.util.compress.ICompress

private val logger = io.github.oshai.kotlinlogging.KotlinLogging.logger {}

internal val json: Json
  get() = Yabapi.defaultJson.value

object Yabapi {
  val defaultJson: AtomicRef<Json> = atomic(
    Json {
      ignoreUnknownKeys = true
      prettyPrint = true
      isLenient = true
      coerceInputValues = true
    },
  )

  val brotliImpl: AtomicRef<ICompress?> = atomic(null)
}

internal inline fun <reified T> String.deserializeJson(): T {
  logger.trace { "Received raw json: $this" }
  return try {
    Yabapi.defaultJson.value.decodeFromString(this)
  } catch (e: SerializationException) {
    logger.error { "Failed to deserialize, raw json: $this" }
    throw e
  }
}

package me.hbj.bikkuri

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.UserAgent
import io.ktor.client.features.compression.ContentEncoding
import io.ktor.client.features.cookies.HttpCookies
import io.ktor.client.features.websocket.WebSockets
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonBuilder
import me.hbj.bikkuri.util.BrotliImpl
import moe.sdl.yabapi.BiliClient
import moe.sdl.yabapi.Yabapi
import moe.sdl.yabapi.enums.LogLevel
import moe.sdl.yabapi.storage.FileCookieStorage
import mu.KotlinLogging
import okio.Path.Companion.toOkioPath

private val logger = KotlinLogging.logger {}

// Safari + MacOS User Agent
private const val WEB_USER_AGENT: String =
  "Mozilla/5.0 (Macintosh; Intel Mac OS X 12_2) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.2 Safari/605.1.15"

internal val client by lazy {
  val httpClient = HttpClient(CIO) {
    install(UserAgent) {
      agent = WEB_USER_AGENT
    }
    install(WebSockets) {
      this.pingInterval = 500
    }
    install(ContentEncoding) {
      gzip()
      deflate()
      identity()
    }
    install(HttpCookies) {
      val file = Bikkuri.resolveDataFile("cookies.json")
      storage = FileCookieStorage(okio.FileSystem.SYSTEM, file.toOkioPath()) {
        saveInTime = true
      }
    }
  }
  BiliClient(httpClient)
}

@Suppress("NOTHING_TO_INLINE")
private inline fun JsonBuilder.buildDefault() {
  isLenient = true
  coerceInputValues = true
  ignoreUnknownKeys = true
  encodeDefaults = true
}

internal val json by lazy {
  Json {
    buildDefault()
  }
}

internal val prettyPrintJson = Json {
  buildDefault()
  prettyPrint = true
}

internal fun initYabapi() = Yabapi.apply {
  defaultJson.getAndSet(json)

  log.getAndSet { tag: String, level: LogLevel, e: Throwable?, message: () -> String ->
    when (level) {
      LogLevel.VERBOSE -> logger.trace { "$tag - ${message()}" }
      LogLevel.DEBUG -> logger.debug { "$tag - ${message()}" }
      LogLevel.INFO -> logger.info { "$tag - ${message()}" }
      LogLevel.WARN -> logger.warn { "$tag - ${message()}" }
      LogLevel.ERROR -> logger.error { "$tag - ${message()}" }
      LogLevel.ASSERT -> logger.error { "-----ASSERT----- $tag - ${message()}" }
    }
  }

  brotliImpl.getAndSet(BrotliImpl)
}

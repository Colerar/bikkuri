package me.hbj.bikkuri

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.UserAgent
import io.ktor.client.features.compression.ContentEncoding
import io.ktor.client.features.cookies.HttpCookies
import io.ktor.client.features.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonBuilder
import moe.sdl.yabapi.BiliClient
import moe.sdl.yabapi.Yabapi
import moe.sdl.yabapi.storage.FileCookieStorage
import okio.Path.Companion.toOkioPath

// Safari + MacOS User Agent
private const val WEB_USER_AGENT: String =
  "Mozilla/5.0 (Macintosh; Intel Mac OS X 12_2) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.2 Safari/605.1.15"

internal val client by lazy {
  val httpClient = HttpClient(CIO) {
    install(UserAgent) {
      agent = WEB_USER_AGENT
    }
    install(ContentEncoding) {
      gzip()
      deflate()
      identity()
    }
    install(HttpCookies) {
      val file = Bikkuri.resolveDataFile("sdl.moe.yabapi/cookies.json")
      storage = FileCookieStorage(okio.FileSystem.SYSTEM, file.toOkioPath())
    }
    defaultRequest {
      header(HttpHeaders.Accept, "*/*")
      header(HttpHeaders.AcceptCharset, "UTF-8")
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
}

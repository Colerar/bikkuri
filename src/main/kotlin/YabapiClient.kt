package me.hbj.bikkuri

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.compression.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.websocket.*
import kotlinx.serialization.json.Json
import me.hbj.bikkuri.bili.BiliClient
import me.hbj.bikkuri.bili.Yabapi
import me.hbj.bikkuri.bili.storage.FileCookieStorage
import me.hbj.bikkuri.utils.resolveDataDirectory
import okio.Path.Companion.toOkioPath

// Safari + MacOS User Agent
private const val WEB_USER_AGENT: String =
  "Mozilla/5.0 (Macintosh; Intel Mac OS X 12_2) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.2 Safari/605.1.15"

internal val client by lazy {
  Yabapi.defaultJson.getAndSet(
    Json {
      isLenient = true
      coerceInputValues = true
      ignoreUnknownKeys = true
    },
  )
  val httpClient = HttpClient(OkHttp) {
    install(UserAgent) {
      agent = WEB_USER_AGENT
    }
    install(WebSockets) {
      this.pingInterval = 500
    }
    install(ContentEncoding) {
      gzip()
      deflate()
    }
    install(HttpCookies) {
      val file = resolveDataDirectory("cookies.json")
      storage = FileCookieStorage(okio.FileSystem.SYSTEM, file.toOkioPath()) {
        saveInTime = true
      }
    }
  }
  BiliClient(httpClient)
}

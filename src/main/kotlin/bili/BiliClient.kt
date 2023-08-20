package me.hbj.bikkuri.bili

import io.ktor.client.*
import io.ktor.client.plugins.cookies.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

class BiliClient(
  var client: HttpClient,
  val context: CoroutineContext = Dispatchers.IO + CoroutineName("yabapi"),
) {
  private suspend fun getBiliCookies(): List<Cookie> = client.cookies("https://.bilibili.com")

  private suspend fun getCsrfToken(): Cookie? = getBiliCookies().firstOrNull { it.name == "bili_jct" }

  internal suspend fun ParametersBuilder.putCsrf(key: String = "csrf") {
    val csrf = getCsrfToken()?.value
    requireNotNull(csrf)
    append(key, csrf)
  }
}

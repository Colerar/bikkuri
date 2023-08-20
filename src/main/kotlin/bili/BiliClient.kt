package me.hbj.bikkuri.bili

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cookies.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import me.hbj.bikkuri.utils.encoding.md5
import java.net.URLEncoder
import kotlin.coroutines.CoroutineContext

@Volatile
var wbiMixinKey: String? = null

class BiliClient(
  var client: HttpClient,
  val context: CoroutineContext = Dispatchers.IO + CoroutineName("yabapi"),
) {
  init {
    client.plugin(HttpSend).intercept { request ->
      if (request.method != HttpMethod.Get &&
        !request.url.host.contains("bilibili") ||
        !request.url.pathSegments.any { it == "wbi" }
      ) {
        return@intercept execute(request)
      }
      val params = request.url.parameters
      val sorted = params.names().associateWith { params[it] }
        .toSortedMap()
      val full = sorted.map { (k, v) ->
        "$k=${URLEncoder.encode(v ?: "", Charsets.UTF_8)}"
      }.joinToString("&")
      val sign = "$full$wbiMixinKey".md5()
      params.clear()
      sorted.forEach { (k, v) ->
        params.append(k, v ?: "")
      }
      params.append("w_rid", sign)

      execute(request)
    }
  }

  suspend fun getBiliCookies(): List<Cookie> = client.cookies("https://.bilibili.com")

  suspend fun getCsrfToken(): Cookie? = getBiliCookies().firstOrNull { it.name == "bili_jct" }

  suspend fun getBuvid3(): Cookie? = getBiliCookies().firstOrNull { it.name == "buvid3" }

  fun getMixinKey(imgKey: String, subKey: String): String {
    val concat = imgKey + subKey
    val key = StringBuilder(32)
    for (i in 0..<32) {
      key.append(concat[mixinKeyEncTab[i]])
    }
    return key.toString()
  }

  internal suspend fun ParametersBuilder.putCsrf(key: String = "csrf") {
    val csrf = getCsrfToken()?.value
    requireNotNull(csrf)
    append(key, csrf)
  }
}

fun wbiMixin() {
}

private val mixinKeyEncTab = intArrayOf(
  46, 47, 18, 2, 53, 8, 23, 32, 15, 50, 10, 31, 58, 3, 45, 35, 27, 43, 5, 49,
  33, 9, 42, 19, 29, 28, 14, 39, 12, 38, 41, 13, 37, 48, 7, 16, 24, 55, 40,
  61, 26, 17, 0, 1, 60, 51, 30, 4, 22, 25, 54, 21, 56, 59, 6, 63, 57, 62, 11,
  36, 20, 34, 44, 52,
)

package me.hbj.bikkuri.bili.data

import io.ktor.http.*
import io.ktor.util.date.*
import kotlinx.serialization.Serializable
import me.hbj.bikkuri.bili.serializer.data.GMTDateSerializer

@Serializable
data class CookieWrapper(
  val name: String,
  val value: String,
  val encoding: CookieEncoding = CookieEncoding.URI_ENCODING,
  val maxAge: Int = 0,
  @Serializable(with = GMTDateSerializer::class)
  val expires: GMTDate? = null,
  val domain: String? = null,
  val path: String? = null,
  val secure: Boolean = false,
  val httpOnly: Boolean = false,
  val extensions: Map<String, String?> = emptyMap(),
) {
  companion object {
    fun fromCookie(cookie: Cookie): CookieWrapper =
      CookieWrapper(
        cookie.name,
        cookie.value,
        cookie.encoding,
        cookie.maxAge,
        cookie.expires,
        cookie.domain,
        cookie.path,
        cookie.secure,
        cookie.httpOnly,
        cookie.extensions,
      )

    fun fromCookies(cookies: List<Cookie>): List<CookieWrapper> =
      cookies.fold(mutableListOf()) { acc, c ->
        acc.add(fromCookie(c))
        acc
      }
  }

  fun toCookie(): Cookie =
    Cookie(name, value, encoding, maxAge, expires, domain, path, secure, httpOnly, extensions)
}

fun Collection<CookieWrapper>.toCookies(): List<Cookie> =
  this.fold(mutableListOf()) { acc, c ->
    acc.add(c.toCookie())
    acc
  }

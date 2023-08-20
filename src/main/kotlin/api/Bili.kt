@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package me.hbj.bikkuri.api

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.withContext
import me.hbj.bikkuri.bili.BiliClient
import me.hbj.bikkuri.bili.consts.internal.MAIN
import me.hbj.bikkuri.bili.data.info.UserSpaceGetResponse
import me.hbj.bikkuri.bili.deserializeJson
import kotlin.coroutines.CoroutineContext

internal const val USER_SPACE_GET_WBI_URL = "$MAIN/x/space/wbi/acc/info"

suspend fun BiliClient.getUserSpaceWbi(
  mid: Long,
  context: CoroutineContext = this.context,
): UserSpaceGetResponse = withContext(context) {
  client.get(USER_SPACE_GET_WBI_URL) {
    parameter("mid", mid.toString())
  }.body<String>().deserializeJson<UserSpaceGetResponse>()
}

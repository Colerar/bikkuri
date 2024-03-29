package me.hbj.bikkuri.bili.data.live

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import me.hbj.bikkuri.bili.data.live.LivePwdCode.UNKNOWN

@Serializable
data class LiveRoomPwdResponse(
  @SerialName("code") val code: LivePwdCode = UNKNOWN,
  @SerialName("msg") val msg: String? = null,
  @SerialName("message") val message: String? = null,
  @SerialName("data") val data: JsonElement? = null,
)

@Serializable
enum class LivePwdCode {
  UNKNOWN,

  @SerialName("0")
  SUCCESS,

  @SerialName("-1")
  PWD_MISMATCHED,
}

package me.hbj.bikkuri.bili.data.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import me.hbj.bikkuri.bili.data.GeneralCode
import me.hbj.bikkuri.bili.data.GeneralCode.UNKNOWN

@Serializable
data class ModifyMsgSettingResponse(
  @SerialName("code") val code: GeneralCode = UNKNOWN,
  @SerialName("msg") val msg: String? = null,
  @SerialName("message") val message: String? = null,
  @SerialName("ttl") val ttl: Int? = null,
  @SerialName("data") val data: JsonElement? = null,
)

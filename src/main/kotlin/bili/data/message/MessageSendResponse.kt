package me.hbj.bikkuri.bili.data.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import me.hbj.bikkuri.bili.data.GeneralCode
import me.hbj.bikkuri.bili.data.GeneralCode.UNKNOWN

@Serializable
data class MessageSendResponse(
  @SerialName("code") val code: GeneralCode = UNKNOWN,
  @SerialName("message") val message: String? = null,
  @SerialName("ttl") val ttl: Int? = null,
  @SerialName("data") val data: MessageSendResponseData? = null,
)

@Serializable
data class MessageSendResponseData(
  @SerialName("msg_key") val key: Long? = null,
  @SerialName("msg_content") val content: String? = null,
  @SerialName("key_hit_infos") val infos: JsonObject? = null,
)

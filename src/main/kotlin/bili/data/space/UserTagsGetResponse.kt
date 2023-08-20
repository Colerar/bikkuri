package me.hbj.bikkuri.bili.data.space

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.hbj.bikkuri.bili.data.GeneralCode
import me.hbj.bikkuri.bili.data.GeneralCode.UNKNOWN

@Serializable
data class UserTagsGetResponse(
  @SerialName("code") val code: GeneralCode = UNKNOWN,
  @SerialName("message") val message: String? = null,
  @SerialName("ttl") val ttl: Int? = null,
  @SerialName("data") val data: List<UserTagsData> = emptyList(),
)

@Serializable
data class UserTagsData(
  @SerialName("mid") val mid: Long? = null,
  @SerialName("tags") val tags: List<String> = emptyList(),
)

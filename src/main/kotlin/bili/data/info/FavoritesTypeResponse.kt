package me.hbj.bikkuri.bili.data.info

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.hbj.bikkuri.bili.data.GeneralCode
import me.hbj.bikkuri.bili.data.GeneralCode.UNKNOWN

@Serializable
data class FavoritesTypeResponse(
  @SerialName("code") val code: GeneralCode = UNKNOWN,
  @SerialName("message") val message: String? = null,
  @SerialName("ttl") val ttl: Int? = null,
  @SerialName("data") val data: List<FavoritesType> = emptyList(),
)

@Serializable
data class FavoritesType(
  @SerialName("tid") val tid: Long? = null,
  @SerialName("name") val name: String? = null,
  @SerialName("count") val count: Int? = null,
)

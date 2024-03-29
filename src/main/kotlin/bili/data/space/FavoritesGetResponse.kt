package me.hbj.bikkuri.bili.data.space

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import me.hbj.bikkuri.bili.data.GeneralCode
import me.hbj.bikkuri.bili.data.GeneralCode.UNKNOWN

@Serializable
data class FavoritesGetResponse(
  @SerialName("code") val code: GeneralCode = UNKNOWN,
  @SerialName("message") val message: String? = null,
  @SerialName("ttl") val ttl: Int? = null,
  @SerialName("data") val data: FavoritesData? = null,
)

@Serializable
data class FavoritesData(
  @SerialName("count") val count: Int? = null,
  @SerialName("list") val list: List<FavoritesItem> = emptyList(),
  @SerialName("season") val season: JsonElement? = null,
)

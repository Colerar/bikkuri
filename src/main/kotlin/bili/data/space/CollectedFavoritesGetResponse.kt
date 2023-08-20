package me.hbj.bikkuri.bili.data.space

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.hbj.bikkuri.bili.data.GeneralCode
import me.hbj.bikkuri.bili.data.GeneralCode.UNKNOWN

@Serializable
data class CollectedFavoritesGetResponse(
  @SerialName("code") val code: GeneralCode = UNKNOWN,
  @SerialName("message") val message: String? = null,
  @SerialName("ttl") val ttl: Int? = null,
  @SerialName("data") val data: CollectedFavoritesData? = null,
)

@Serializable
data class CollectedFavoritesData(
  @SerialName("count") val count: Int? = null,
  @SerialName("list") val list: List<CollectedFavoritesItem> = emptyList(),
  @SerialName("has_more") val hasMore: Boolean? = null,
)

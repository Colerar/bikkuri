package me.hbj.bikkuri.bili.data.space

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class BaseFavoritesItem {
  @SerialName("id")
  abstract val id: Long?

  @SerialName("fid")
  abstract val fid: Long?

  @SerialName("mid")
  abstract val mid: Long?

  @SerialName("attr")
  abstract val attribute: Int?

  @SerialName("title")
  abstract val title: String?

  @SerialName("fav_state")
  abstract val favState: Int?

  @SerialName("media_count")
  abstract val count: Int?
}

@Serializable
data class FavoritesItem(
  @SerialName("id") override val id: Long? = null,
  @SerialName("fid") override val fid: Long? = null,
  @SerialName("mid") override val mid: Long? = null,
  @SerialName("attr") override val attribute: Int? = null,
  @SerialName("title") override val title: String? = null,
  @SerialName("fav_state") override val favState: Int? = null,
  @SerialName("media_count") override val count: Int? = null,
) : BaseFavoritesItem()

@Serializable
data class CollectedFavoritesItem(
  @SerialName("id") override val id: Long? = null,
  @SerialName("fid") override val fid: Long? = null,
  @SerialName("mid") override val mid: Long? = null,
  @SerialName("attr") override val attribute: Int? = null,
  @SerialName("title") override val title: String? = null,
  @SerialName("fav_state") override val favState: Int? = null,
  @SerialName("media_count") override val count: Int? = null,
  @SerialName("cover") val cover: String? = null,
  @SerialName("upper") val upper: FavoritesOwner? = null,
  @SerialName("cover_type") val coverType: Int? = null,
  @SerialName("intro") val intro: String? = null,
  @SerialName("ctime") val createdTime: Long? = null,
  @SerialName("mtime") val modifiedTime: Long? = null,
  @SerialName("state") val state: Int? = null,
  @SerialName("view_count") val viewCount: Int? = null,
  @SerialName("type") val type: Int? = null,
  @SerialName("link") val link: String? = null,
) : BaseFavoritesItem()

@Serializable
data class FavoritesOwner(
  @SerialName("mid") val mid: Long? = null,
  @SerialName("name") val name: String? = null,
  @SerialName("face") val face: String? = null,
)

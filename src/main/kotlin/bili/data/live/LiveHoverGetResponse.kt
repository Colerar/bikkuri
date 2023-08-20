@file:UseSerializers(BooleanJsSerializer::class)

package me.hbj.bikkuri.bili.data.live

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import me.hbj.bikkuri.bili.data.GeneralCode
import me.hbj.bikkuri.bili.data.GeneralCode.UNKNOWN
import me.hbj.bikkuri.bili.serializer.BooleanJsSerializer

@Serializable
data class LiveHoverGetResponse(
  @SerialName("code") val code: GeneralCode = UNKNOWN,
  @SerialName("msg") val msg: String? = null,
  @SerialName("message") val message: String? = null,
  @SerialName("data") val data: LiveHover? = null,
)

@Serializable
data class LiveHover(
  @SerialName("list") val room: List<Room> = emptyList(),
  @SerialName("banner") val banner: List<Banner> = emptyList(),
) {
  @Serializable
  data class Room(
    @SerialName("area_id") val areaId: Long? = null,
    @SerialName("area_name") val areaName: String? = null,
    @SerialName("pic") val pic: String? = null,
    @SerialName("is_hot") val isHot: Boolean? = null,
    @SerialName("is_new") val isNew: Boolean? = null,
    @SerialName("update_time") val updateTime: String? = null,
  )

  @Serializable
  data class Banner(
    @SerialName("id") val id: Long? = null,
    @SerialName("img") val img: String? = null,
    @SerialName("url") val url: String? = null,
  )
}

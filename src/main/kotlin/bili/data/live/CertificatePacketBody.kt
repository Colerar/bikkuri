package me.hbj.bikkuri.bili.data.live

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CertificatePacketBody(
  @SerialName("uid") val mid: Long,
  @SerialName("roomid") val roomId: Long,
  @SerialName("key") val key: String,
  @SerialName("protover") val version: Int,
  @SerialName("platform") val platform: String,
  @SerialName("buvid") val buvid: String?,
  @SerialName("type") val type: Int,
)

package me.hbj.bikkuri.bili.data.space

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SpaceVideoButton(
  @SerialName("text") val text: String? = null,
  @SerialName("uri") val playUri: String? = null, // like: //www.bilibili.com/medialist/play/$mid?from=space
)

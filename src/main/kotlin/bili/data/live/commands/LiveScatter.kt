package me.hbj.bikkuri.bili.data.live.commands

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LiveScatter(
  @SerialName("max") val max: Int? = null,
  @SerialName("min") val min: Int? = null,
)

package me.hbj.bikkuri.bili.data.live

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class LiveRoomStatus {
  UNKNOWN,

  @SerialName("0")
  NOT_LIVE,

  @SerialName("1")
  LIVING,

  @SerialName("2")
  ROUNDING,
}

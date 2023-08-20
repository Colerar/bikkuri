package me.hbj.bikkuri.bili.data.live

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class LiveResponseCode {
  UNKNOWN,

  @SerialName("0")
  SUCCESS,

  @SerialName("1")
  ERROR,

  @SerialName("60004")
  NOT_FOUND,

  @SerialName("60009")
  INVALID_LIVE_TYPE,
}

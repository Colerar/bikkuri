package me.hbj.bikkuri.bili.data.live.commands

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class LotStatus {
  UNKNOWN,

  @SerialName("0")
  START,

  @SerialName("2")
  AWARDED,

  @SerialName("4")
  REVIEW_PASS,

  @SerialName("5")
  REVIEW_FAILED,
}

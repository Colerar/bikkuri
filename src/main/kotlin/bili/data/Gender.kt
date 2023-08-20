package me.hbj.bikkuri.bili.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Gender {
  UNKNOWN,

  @SerialName("-1")
  PRIVATE,

  @SerialName("0")
  FEMALE,

  @SerialName("1")
  MALE,
}

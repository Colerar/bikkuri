package me.hbj.bikkuri.bili.data.live

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.hbj.bikkuri.bili.data.GeneralCode
import me.hbj.bikkuri.bili.data.GeneralCode.UNKNOWN

@Serializable
data class RoomIdByUserResponse(
  @SerialName("code") val code: GeneralCode = UNKNOWN,
  @SerialName("msg") val msg: String? = null,
  @SerialName("message") val message: String? = null,
  @SerialName("data") val data: RoomId? = null,
) {
  // shortcut
  inline val roomId: Long?
    get() = data?.roomId

  @Serializable
  data class RoomId(
    @SerialName("room_id") val roomId: Long? = null,
  )
}

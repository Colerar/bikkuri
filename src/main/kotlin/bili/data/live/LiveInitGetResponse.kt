@file:UseSerializers(BooleanJsSerializer::class)

package me.hbj.bikkuri.bili.data.live

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import me.hbj.bikkuri.bili.Yabapi
import me.hbj.bikkuri.bili.data.live.LiveResponseCode.SUCCESS
import me.hbj.bikkuri.bili.data.live.LiveResponseCode.UNKNOWN
import me.hbj.bikkuri.bili.serializer.BooleanJsSerializer

@Serializable
data class LiveInitGetResponse(
  @SerialName("code") val code: LiveResponseCode = UNKNOWN,
  @SerialName("msg") val msg: String? = null,
  @SerialName("message") val message: String? = null,
  @SerialName("data") private val _data: JsonElement? = null,
) {
  val data: LiveInitData? by lazy {
    if (code != SUCCESS) return@lazy null
    _data?.let {
      Yabapi.defaultJson.value.decodeFromJsonElement(it)
    }
  }
}

@Serializable
data class LiveInitData(
  @SerialName("room_id") val roomId: Long? = null,
  @SerialName("short_id") val shortId: Long? = null,
  @SerialName("uid") val uid: Long? = null,
  @SerialName("need_p2p") val needP2p: Boolean? = null,
  @SerialName("is_hidden") val isHidden: Boolean? = null,
  @SerialName("is_locked") val isLocked: Boolean? = null,
  @SerialName("is_portrait") val isPortrait: Boolean? = null,
  @SerialName("live_status") val liveStatus: Boolean? = null,
  @SerialName("hidden_till") val hiddenTill: Long? = null,
  @SerialName("lock_till") val lockTill: Long? = null,
  @SerialName("encrypted") val encrypted: Boolean? = null,
  @SerialName("pwd_verified") val pwdVerified: Boolean? = null,
  @SerialName("live_time") val liveTime: Long? = null,
  @SerialName("room_shield") val roomShield: Int? = null,
  @SerialName("is_sp") val isSpecial: Boolean? = null,
  @SerialName("special_type") val specialType: SpecialType = SpecialType.UNKNOWN,
)

@Serializable
enum class SpecialType {
  UNKNOWN,

  @SerialName("0")
  NORMAL,

  @SerialName("1")
  PAID,

  @SerialName("2")
  NEW_YEAR,
}

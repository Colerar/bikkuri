@file:UseSerializers(BooleanJsSerializer::class)

package me.hbj.bikkuri.bili.data.live.commands

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import me.hbj.bikkuri.bili.data.live.GuardLevel
import me.hbj.bikkuri.bili.serializer.BooleanJsSerializer

@Serializable
data class LiveInteractGameCmd(
  @SerialName("cmd") override val operation: String,
  @SerialName("data") val data: LiveGameData? = null,
) : LiveCommand {
  companion object : LiveCommandFactory() {
    override val operation: String = "LIVE_INTERACTIVE_GAME"
    override fun decode(json: Json, data: JsonElement): LiveInteractGameCmd = json.decodeFromJsonElement(data)
  }
}

@Serializable
data class LiveGameData(
  @SerialName("type") val type: Int? = null,
  @SerialName("uid") val uid: Long? = null,
  @SerialName("uname") val userName: String? = null,
  @SerialName("uface") val userAvatar: String? = null,
  @SerialName("gift_id") val giftId: Long? = null,
  @SerialName("gift_name") val giftName: String? = null,
  @SerialName("gift_num") val giftNum: Int? = null,
  @SerialName("price") val price: Int? = null,
  @SerialName("paid") val paid: Boolean? = null,
  @SerialName("msg") val msg: String? = null,
  @SerialName("fans_medal_level") val fansMedalLevel: Int? = null,
  @SerialName("guard_level") val guardLevel: GuardLevel? = null,
  @SerialName("timestamp") val timestamp: Long? = null,
  @SerialName("anchor_lottery") val anchorLottery: JsonElement? = null,
  @SerialName("pk_info") val pkInfo: JsonElement? = null,
  @SerialName("anchor_info") val anchorInfo: AnchorInfo? = null,
) {
  @Serializable
  data class AnchorInfo(
    @SerialName("uid") val uid: Long? = null,
    @SerialName("uname") val userName: String? = null,
    @SerialName("uface") val userAvatar: String? = null,
  )
}

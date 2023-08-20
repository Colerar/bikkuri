package me.hbj.bikkuri.bili.data.live.commands

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import me.hbj.bikkuri.bili.data.RgbColor
import me.hbj.bikkuri.bili.data.live.commands.LotStatus.UNKNOWN
import me.hbj.bikkuri.bili.serializer.data.RgbColorIntSerializerNullable

@Serializable
data class LotAwardCmd(
  @SerialName("cmd") override val operation: String,
  @SerialName("data") val data: LotAwardData? = null,
) : LiveCommand {
  companion object : LiveCommandFactory() {
    override val operation: String = "ANCHOR_LOT_AWARD"
    override fun decode(json: Json, data: JsonElement): LotAwardCmd = json.decodeFromJsonElement(data)
  }
}

@Serializable
data class LotAwardData(
  @SerialName("award_image") val image: String? = null,
  @SerialName("award_name") val name: String? = null,
  @SerialName("award_num") val count: Int? = null, // 應該是獎品個數
  @SerialName("award_users") val users: List<LotAwardUser> = emptyList(),
  @SerialName("id") val id: Long? = null,
  @SerialName("lot_status") val status: LotStatus = UNKNOWN,
  @SerialName("url") val url: String? = null,
  @SerialName("web_url") val webUrl: String? = null,
)

@Serializable
data class LotAwardUser(
  @SerialName("uid") val uid: Long? = null,
  @SerialName("uname") val uname: String? = null,
  @SerialName("face") val avatar: String? = null,
  @SerialName("level") val level: Int? = null,
  @Serializable(RgbColorIntSerializerNullable::class)
  @SerialName("color") val color: RgbColor? = null,
)

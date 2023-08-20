@file:UseSerializers(BooleanJsSerializer::class)

package me.hbj.bikkuri.bili.data.live.commands

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import me.hbj.bikkuri.bili.data.RgbColor
import me.hbj.bikkuri.bili.data.live.GuardLevel
import me.hbj.bikkuri.bili.data.live.GuardLevel.UNKNOWN
import me.hbj.bikkuri.bili.serializer.BooleanJsSerializer
import me.hbj.bikkuri.bili.serializer.data.RgbColorIntSerializerNullable

@Serializable
data class InteractWordCmd(
  @SerialName("cmd") override val operation: String,
  @SerialName("data") val data: InteractWordData? = null,
) : LiveCommand {
  companion object : LiveCommandFactory() {
    override val operation: String = "INTERACT_WORD"
    override fun decode(json: Json, data: JsonElement): InteractWordCmd = json.decodeFromJsonElement(data)
  }
}

@Serializable
data class InteractWordData(
  @SerialName("contribution") val contribution: Contribution? = null,
  @SerialName("dmscore") val danmakuScore: Int? = null,
  @SerialName("fans_medal") val fansMedal: LiveMedal? = null,
  @SerialName("identities") val identities: List<Int> = emptyList(),
  @SerialName("is_spread") val isSpread: Boolean? = null,
  @SerialName("msg_type") val msgType: Int? = null,
  @SerialName("privilege_type") val privilegeType: Int? = null,
  @SerialName("roomid") val roomId: Long? = null,
  @SerialName("score") val score: Long? = null,
  @SerialName("spread_desc") val spreadDesc: String? = null,
  @SerialName("spread_info") val spreadInfo: String? = null,
  @SerialName("tail_icon") val tailIcon: Int? = null,
  @SerialName("timestamp") val timestamp: Long? = null,
  @SerialName("trigger_time") val triggerTime: Long? = null,
  @SerialName("uid") val uid: Long? = null,
  @SerialName("uname") val userName: String? = null,
  @SerialName("uname_color") val userNameColor: String? = null,
) {
  @Serializable
  data class Contribution(
    @SerialName("grade") val grade: Int? = null,
  )

  @Serializable
  data class LiveMedal(
    @SerialName("anchor_roomid") val roomId: Long? = null, // 房间id
    @SerialName("guard_level") val guardLevel: GuardLevel = UNKNOWN, // 等级
    @SerialName("icon_id") val iconId: Long? = null, // icon id
    @SerialName("is_lighted") val isLighted: Boolean? = null, // 是否点亮
    @Serializable(RgbColorIntSerializerNullable::class)
    @SerialName("medal_color") val medalColor: RgbColor? = null,
    @Serializable(RgbColorIntSerializerNullable::class)
    @SerialName("medal_color_border") val medalColorBorder: RgbColor? = null,
    @Serializable(RgbColorIntSerializerNullable::class)
    @SerialName("medal_color_end") val medalColorEnd: RgbColor? = null,
    @Serializable(RgbColorIntSerializerNullable::class)
    @SerialName("medal_color_start") val medalColorStart: RgbColor? = null,
    @SerialName("medal_level") val level: Int? = null,
    @SerialName("medal_name") val name: String? = null,
    @SerialName("score") val score: Int? = null,
    @SerialName("special") val special: String? = null,
    @SerialName("target_id") val targetId: Long? = null, // 主播 mid
  )
}

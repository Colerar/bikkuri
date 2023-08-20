@file:UseSerializers(BooleanJsSerializer::class)

package me.hbj.bikkuri.bili.data.live

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonObject
import me.hbj.bikkuri.bili.data.GeneralCode
import me.hbj.bikkuri.bili.data.GeneralCode.UNKNOWN
import me.hbj.bikkuri.bili.data.RgbColor
import me.hbj.bikkuri.bili.data.live.GuardLevel.NONE
import me.hbj.bikkuri.bili.serializer.BooleanJsSerializer
import me.hbj.bikkuri.bili.serializer.data.RgbColorIntSerializerNullable

@Serializable
data class LiveGuardListGetResponse(
  @SerialName("code") val code: GeneralCode = UNKNOWN,
  @SerialName("message") val message: String? = null,
  @SerialName("ttl") val ttl: Int? = null,
  @SerialName("data") val data: LiveGuardPage? = null,
)

@Serializable
data class LiveGuardPage(
  @SerialName("info") val info: PageInfo? = null,
  @SerialName("list") val list: List<GuardNode> = emptyList(),
  @SerialName("top3") val top3: List<GuardNode> = emptyList(),
  // no valuable info, all number and string filled with zero
  @SerialName("my_follow_info") val myFollowInfo: JsonObject? = null,
  @SerialName("guard_warn") val guardWarn: JsonObject? = null,
  @SerialName("exist_benefit") val existBenefit: Boolean? = null,
  @SerialName("remind_benefit") val remindBenefit: String? = null,
) {
  @Serializable
  data class PageInfo(
    @SerialName("num") val num: Int? = null,
    @SerialName("page") val page: Long? = null,
    @SerialName("now") val now: Int? = null,
    @SerialName("achievement_level") val achievementLevel: Int? = null,
    @SerialName("anchor_guard_achieve_level") val anchorGuardAchieveLevel: Int? = null,
  )
}

@Serializable
data class GuardNode(
  @SerialName("uid") val uid: Long? = null,
  @SerialName("ruid") val liverUid: Long? = null,
  @SerialName("rank") val rank: Int? = null,
  @SerialName("username") val username: String? = null,
  @SerialName("face") val face: String? = null,
  @SerialName("is_alive") val isAlive: Boolean? = null,
  @SerialName("guard_level") val guardLevel: GuardLevel = NONE,
  @SerialName("guard_sub_level") val guardSubLevel: Int? = null,
  @SerialName("medal_info") val medalInfo: MedalInfo? = null,
)

@Serializable
data class MedalInfo(
  @SerialName("medal_name") val medalName: String? = null,
  @SerialName("medal_level") val medalLevel: Int? = null,
  @Serializable(RgbColorIntSerializerNullable::class)
  @SerialName("medal_color_start") val medalColorStart: RgbColor? = null,
  @Serializable(RgbColorIntSerializerNullable::class)
  @SerialName("medal_color_end") val medalColorEnd: RgbColor? = null,
  @Serializable(RgbColorIntSerializerNullable::class)
  @SerialName("medal_color_border") val medalColorBorder: RgbColor? = null,
)

@file:UseSerializers(BooleanJsSerializer::class)

package me.hbj.bikkuri.bili.data.live.commands

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import me.hbj.bikkuri.bili.data.RgbColor
import me.hbj.bikkuri.bili.data.live.BatteryCurrency
import me.hbj.bikkuri.bili.data.live.GuardLevel
import me.hbj.bikkuri.bili.serializer.BooleanJsSerializer
import me.hbj.bikkuri.bili.serializer.data.RgbColorStringSerializerNullable

@Serializable
data class UserToastMsgCmd(
  @SerialName("cmd") override val operation: String,
  @SerialName("data") val data: UserToast,
) : LiveCommand {
  companion object : LiveCommandFactory() {
    override val operation: String = "USER_TOAST_MSG"

    override fun decode(json: Json, data: JsonElement): LiveCommand =
      json.decodeFromJsonElement<UserToastMsgCmd>(data)
  }
}

@Serializable
data class UserToast(
  @SerialName("is_show") val isShow: Boolean, // 是否显示
  @SerialName("anchor_show") val showAnchor: Boolean, // 应该是是否显示上舰动画
  @Serializable(RgbColorStringSerializerNullable::class)
  @SerialName("color") val color: RgbColor? = null, // 可能是底色 目前观察到的(舰长)都为 #00D1F1
  @SerialName("dmscore") val score: Int,
  @SerialName("start_time") val startTime: Long, // 开始显示时间
  @SerialName("end_time") val endTime: Long, // 结束时间
  @SerialName("guard_level") val guardLevel: GuardLevel, // 等级
  @SerialName("num") val num: Int, // 上舰数量
  @SerialName("unit") val unit: String, // 时间单位
  @SerialName("username") val username: String, // 用户名
  @SerialName("op_type") private val _operateType: Int, // 1: 開通 2: 續費 3: 自動續費
  @SerialName("role_name") val roleName: String, // 舰长 / 提督 / 总督
  @SerialName("price") val price: BatteryCurrency, // 花费 单位为电池
  @SerialName("toast_msg") val message: String, // "<%nickname%> 自动续费了舰长"
  @SerialName("payflow_id") val payflowId: String, // 账单号
  @SerialName("svga_block") val svgaBlock: Int, // unknown
  @SerialName("target_guard_count") val targetGuardCount: Int, // 主播目前艦長數量
  @SerialName("uid") val uid: Long, // 用户 uid
  @SerialName("user_show") val showUser: Boolean, // 是否显示用户
) {
  val operateType: ToastOperateType by lazy { ToastOperateType.fromCode(_operateType) }
}

@Serializable
enum class ToastOperateType(val code: Int?) {
  UNKNOWN(null),
  NEW(1),
  RENEW(2),
  AUTO_RENEW(3),
  ;

  companion object {
    fun fromCode(code: Int): ToastOperateType = values().firstOrNull { it.code == code } ?: UNKNOWN
  }
}

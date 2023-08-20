@file:UseSerializers(BooleanJsSerializer::class)

package me.hbj.bikkuri.bili.data.info

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import me.hbj.bikkuri.bili.data.RgbColor
import me.hbj.bikkuri.bili.data.info.VipStatus.*
import me.hbj.bikkuri.bili.serializer.BooleanJsSerializer
import me.hbj.bikkuri.bili.serializer.data.RgbColorStringSerializerNullable

/**
 * 大会员数据类
 * @param type 大会员类型 [VipType] 月度/年度
 * @param status 大会员状态 [VipStatus] 正常/异常/冻结
 * @param dueDate 到期时间 单位: ms
 * @param isPaid 是否购买
 * @param themeType 未知
 * @param label [VipLabel]
 * @param role 未知
 * @param avatarSubscriptUrl 頭像下標, 如黃色閃電, 大會員
 */
@Serializable
data class UserVip(
  @SerialName("type") val type: VipType = VipType.UNKNOWN,
  @SerialName("status") val status: VipStatus = UNKNOWN,
  @SerialName("due_date") val dueDate: Long? = null,
  @SerialName("vip_pay_type") val isPaid: Boolean? = null,
  @SerialName("theme_type") val themeType: Int? = null,
  @SerialName("label") val label: VipLabel? = null,
  @SerialName("avatar_subscript") val isShowSubscript: Boolean? = null,
  @Serializable(RgbColorStringSerializerNullable::class)
  @SerialName("nickname_color") val nicknameColor: RgbColor? = null,
  @SerialName("role") val role: Int? = null,
  @SerialName("avatar_subscript_url") val avatarSubscriptUrl: String? = null,
  @SerialName("tv_vip_status") val tvVipStatus: Int? = null,
  @SerialName("tv_vip_pay_type") val tvVipPayType: Int? = null,
)

@Serializable
enum class VipType {
  UNKNOWN,

  @SerialName("0")
  NONE,

  @SerialName("1")
  MONTH,

  @SerialName("2")
  YEAR,
}

/**
 * 大会员状态
 * @property UNKNOWN 未知
 * @property NORMAL 正常
 * @property IP_CHANGE_FREQUENT IP 更换频繁被冻结
 * @property RISK_LOCKED 风控冻结
 */
@Serializable
enum class VipStatus {
  UNKNOWN,

  @SerialName("1")
  NORMAL,

  @SerialName("2")
  IP_CHANGE_FREQUENT,

  @SerialName("3")
  RISK_LOCKED,
}

/**
 * @param path 未知
 * @param text 会员名称
 * @param label 标签
 * @param textColor 文字颜色
 * @param backgroundStyle 背景风格
 * @param backgroundColor 背景颜色
 * @param borderColor 描边颜色
 */
@Serializable
data class VipLabel(
  @SerialName("path") val path: String? = null,
  @SerialName("text") val text: String? = null,
  @SerialName("label_theme") val label: String? = null,
  @SerialName("text_color") val textColor: String? = null,
  @SerialName("bg_style") val backgroundStyle: Int? = null,
  @SerialName("bg_color") val backgroundColor: String? = null,
  @SerialName("border_color") val borderColor: String? = null,
  @SerialName("use_img_label") val useImgLabel: Boolean? = null,
  @SerialName("img_label_uri_hans") val imgLabelUriHans: String? = null,
  @SerialName("img_label_uri_hant") val imgLabelUriHant: String? = null,
  @SerialName("img_label_uri_hans_static") val imgLabelUriHansStatic: String? = null,
  @SerialName("img_label_uri_hant_static") val imgLabelUriHantStatic: String? = null,
)

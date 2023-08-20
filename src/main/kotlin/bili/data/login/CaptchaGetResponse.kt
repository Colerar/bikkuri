@file:Suppress("UNUSED")

package me.hbj.bikkuri.bili.data.login

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.hbj.bikkuri.bili.data.GeneralCode
import me.hbj.bikkuri.bili.data.GeneralCode.UNKNOWN

/**
 * Json Response
 * @param code 返回状态码 [GetCaptchaResponse]
 * @param data 数据 [CaptchaData]
 */
@Serializable
data class GetCaptchaResponse(
  @SerialName("code") val code: GeneralCode = UNKNOWN,
  @SerialName("message") val message: String? = null,
  @SerialName("ttl") val ttl: Int? = null,
  @SerialName("data") val data: CaptchaData? = null,
)

@Serializable
data class CaptchaData(
  val type: String? = null,
  val token: String? = null,
  val geetest: GeetestCaptcha? = null,
  val tencent: TencentCaptcha? = null,
)

@Serializable
data class GeetestCaptcha(
  @SerialName("challenge") val challenge: String? = null,
  @SerialName("gt") val gt: String? = null,
)

@Serializable
data class TencentCaptcha(
  @SerialName("appid") val appid: String? = null,
)

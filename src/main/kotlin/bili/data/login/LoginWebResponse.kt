@file:UseSerializers(BooleanJsSerializer::class)

package me.hbj.bikkuri.bili.data.login

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import me.hbj.bikkuri.bili.data.login.LoginWebResponseCode.*
import me.hbj.bikkuri.bili.serializer.BooleanJsSerializer

/**
 * @param code 返回值 [LoginWebResponseCode]
 * @param timestamp 时间戳 仅失败时返回
 * @param message 错误信息, 默认为 "0"
 * @param data 成功时返回
 */
@Serializable
data class LoginWebResponse(
  @SerialName("code")
  val code: LoginWebResponseCode = UNKNOWN,
  @SerialName("ts")
  val timestamp: Long? = null,
  @SerialName("ttl") val ttl: Int? = null,
  @SerialName("message")
  val message: String? = null,
  @SerialName("data")
  val data: LoginWebResponseData? = null,
)

/**
 * [LoginWebResponse] 的返回值
 * @property UNKNOWN 未知返回值
 * @property SUCCESS 成功
 * @property INVALID_REQUEST 请求错误
 * @property INVALID_ACCOUNT 帐号或密码错误
 * @property BLANK_ACCOUNT 帐号或密码为空
 * @property TIMEOUT 超时
 * @property MISS_PARAMETER 缺少必要参数
 * @property NEED_TWO_FACTOR 需要手机或邮箱二要素验证
 * @property INVALID_LOGIN_KEY 登录密钥错误 @see [GetCaptchaResponse]
 * @property CAPTCHA_SERVICE_ERROR 验证码服务错误
 * @property RSA_DECRYPT_FAILED RSA 解密失敗
 */
@Serializable
enum class LoginWebResponseCode {
  UNKNOWN,

  @SerialName("0")
  SUCCESS,

  @SerialName("-400")
  INVALID_REQUEST,

  @SerialName("-629")
  INVALID_ACCOUNT,

  @SerialName("-653")
  BLANK_ACCOUNT,

  @SerialName("-662")
  TIMEOUT,

  @SerialName("-2001")
  MISS_PARAMETER,

  @SerialName("-2100")
  NEED_TWO_FACTOR,

  @SerialName("2400")
  INVALID_LOGIN_KEY,

  @SerialName("2406")
  CAPTCHA_SERVICE_ERROR,

  @SerialName("86000")
  RSA_DECRYPT_FAILED,
}

/**
 * [LoginWebResponse] 的正文数据
 */
@Serializable
data class LoginWebResponseData(
  @SerialName("status") val status: Int? = null,
  @SerialName("message") val message: String? = null,
  @SerialName("url") val url: String? = null,
)

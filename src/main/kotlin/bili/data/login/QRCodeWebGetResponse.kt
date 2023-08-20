@file:UseSerializers(BooleanJsSerializer::class)

package me.hbj.bikkuri.bili.data.login

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import me.hbj.bikkuri.bili.data.GeneralCode
import me.hbj.bikkuri.bili.data.GeneralCode.UNKNOWN
import me.hbj.bikkuri.bili.serializer.BooleanJsSerializer

/**
 * @param code [GeneralCode]
 * @param status 作用不明
 * @param timestamp 时间戳
 * @param data [QRCodeWebGetResponseData]
 */
@Serializable
data class QRCodeWebGetResponse(
  @SerialName("code")
  val code: GeneralCode = UNKNOWN,
  @SerialName("status")
  val status: Boolean? = null,
  @SerialName("ts")
  val timestamp: Long? = null,
  val data: QRCodeWebGetResponseData? = null,
)

/**
 * @param url 二维码内容地址
 * @param oauthKey 扫码登录密钥
 */
@Serializable
data class QRCodeWebGetResponseData(
  val url: String? = null,
  val oauthKey: String? = null,
)

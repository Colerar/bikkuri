@file:UseSerializers(BooleanJsSerializer::class)

package me.hbj.bikkuri.bili.data.live

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import me.hbj.bikkuri.bili.data.GeneralCode
import me.hbj.bikkuri.bili.serializer.BooleanJsSerializer

/**
 * 直播簽到 Response
 */
@Serializable
data class LiveSignResponse(
  @SerialName("code") val code: GeneralCode? = null,
  @SerialName("message") val message: String? = null,
  @SerialName("ttl") val ttl: Int? = null,
  @SerialName("data") val data: LiveSignData? = null,
)

/**
 * 簽到數據
 * @property text 提示
 * @property specialText 特殊提示
 * @property allDays 本月一共多少天
 * @property hadSignDays 簽到過的時間
 * @property isBonusDay 是否獎勵日
 */
@Serializable
data class LiveSignData(
  @SerialName("text") val text: String? = null,
  @SerialName("specialText") val specialText: String? = null,
  @SerialName("allDays") val allDays: Int? = null,
  @SerialName("hadSignDays") val hadSignDays: Int? = null,
  @SerialName("isBonusDay") val isBonusDay: Boolean? = null,
)

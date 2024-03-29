package me.hbj.bikkuri.bili.data.info

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.hbj.bikkuri.bili.data.GeneralCode
import me.hbj.bikkuri.bili.data.GeneralCode.UNKNOWN

/**
 * 实时获取投币经验值
 * @param code 返回值 [GeneralCode]
 * @param coinExp 每日投币经验值, 上限 50
 * @see ExpReward
 */
@Serializable
data class CoinExpGetResponse(
  @SerialName("code") val code: GeneralCode = UNKNOWN,
  @SerialName("message") val message: String? = null,
  @SerialName("number") val coinExp: Int? = null,
)

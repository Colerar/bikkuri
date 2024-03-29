package me.hbj.bikkuri.bili.data.info

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.hbj.bikkuri.bili.data.GeneralCode
import me.hbj.bikkuri.bili.data.GeneralCode.UNKNOWN

/**
 * 獲取硬幣變化的返回
 * @param code [GeneralCode]
 * @param data [CoinLog]
 */
@Serializable
data class CoinLogGetResponse(
  @SerialName("code") val code: GeneralCode = UNKNOWN,
  @SerialName("message") val message: String? = null,
  @SerialName("ttl") val ttl: Int? = null,
  @SerialName("data") val data: CoinLog? = null,
)

/**
 * 硬幣變化記錄
 */
@Serializable
data class CoinLog(
  @SerialName("list") val list: List<CoinLogNode> = emptyList(),
  @SerialName("count") val size: Int? = null,
)

/**
 * @param time 時間, YYYY-MM-DD HH:MM:SS
 * @param changed 變化量 正值收入, 負值支出
 * @param reason 變化說明
 */
@Serializable
data class CoinLogNode(
  @SerialName("time") val time: String? = null,
  @SerialName("delta") val changed: Double? = null,
  @SerialName("reason") val reason: String? = null,
)

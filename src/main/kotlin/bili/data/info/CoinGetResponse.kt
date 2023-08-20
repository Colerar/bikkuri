@file:UseSerializers(BooleanJsSerializer::class)

package me.hbj.bikkuri.bili.data.info

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import me.hbj.bikkuri.bili.data.GeneralCode
import me.hbj.bikkuri.bili.data.GeneralCode.UNKNOWN
import me.hbj.bikkuri.bili.serializer.BooleanJsSerializer

/**
 * 获得硬币数返回
 * @param data [CoinData]
 */
@Serializable
data class CoinGetResponse(
  @SerialName("code") val code: GeneralCode = UNKNOWN,
  @SerialName("status") val status: Boolean? = null,
  @SerialName("data") val data: CoinData? = null,
)

/**
 * @param money 硬币数
 */
@Serializable
data class CoinData(
  @SerialName("money") val money: Double? = null,
)

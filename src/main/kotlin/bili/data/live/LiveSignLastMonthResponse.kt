package me.hbj.bikkuri.bili.data.live

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.hbj.bikkuri.bili.data.GeneralCode
import me.hbj.bikkuri.bili.data.GeneralCode.UNKNOWN

@Serializable
data class LiveSignLastMonthResponse(
  @SerialName("code") val code: GeneralCode = UNKNOWN,
  @SerialName("message") val message: String? = null,
  @SerialName("ttl") val ttl: Int? = null,
  @SerialName("data") val data: LiveSignLastMonthInfo? = null,
)

@Serializable
data class LiveSignLastMonthInfo(
  @SerialName("month") val month: Int? = null,
  @SerialName("days") val days: Int? = null,
  @SerialName("hadSignDays") val hadSignDays: Int? = null,
  @SerialName("signDaysList") val signDaysList: List<Int> = emptyList(),
  @SerialName("signBonusDaysList") val signBonusDaysList: List<Int> = emptyList(),
)

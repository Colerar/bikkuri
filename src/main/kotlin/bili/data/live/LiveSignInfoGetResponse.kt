package me.hbj.bikkuri.bili.data.live

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.hbj.bikkuri.bili.data.GeneralCode
import me.hbj.bikkuri.bili.data.GeneralCode.UNKNOWN

@Serializable
data class LiveSignInfoGetResponse(
  @SerialName("code") val code: GeneralCode = UNKNOWN,
  @SerialName("message") val message: String? = null,
  @SerialName("ttl") val ttl: Int? = null,
  @SerialName("data") val data: LiveSignInfo? = null,
)

@Serializable
data class LiveSignInfo(
  @SerialName("text") val text: String? = null,
  @SerialName("specialText") val specialText: String? = null,
  @SerialName("status") val status: Int? = null,
  @SerialName("allDays") val allDays: Int? = null,
  @SerialName("curMonth") val curMonth: Int? = null,
  @SerialName("curYear") val curYear: Int? = null,
  @SerialName("curDay") val curDay: Int? = null,
  @SerialName("curDate") val curDate: String? = null,
  @SerialName("hadSignDays") val hadSignDays: Int? = null,
  @SerialName("newTask") val newTask: Int? = null,
  @SerialName("signDaysList") val signDaysList: List<Int> = emptyList(),
  @SerialName("signBonusDaysList") val signBonusDaysList: List<Int> = emptyList(),
)

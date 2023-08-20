@file:UseSerializers(BooleanJsSerializer::class)

package me.hbj.bikkuri.bili.data.login

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import me.hbj.bikkuri.bili.data.GeneralCode
import me.hbj.bikkuri.bili.data.GeneralCode.UNKNOWN
import me.hbj.bikkuri.bili.serializer.BooleanJsSerializer

@Serializable
data class LogOutResponse(
  @SerialName("code") val code: GeneralCode = UNKNOWN,
  @SerialName("status") val status: Boolean? = null,
  @SerialName("ts") val timestamp: Long? = null,
  @SerialName("message") val message: String? = null,
  @SerialName("data") val data: LogOutResponseData? = null,
)

@Serializable
data class LogOutResponseData(
  @SerialName("redirectUrl") val redirectUrl: String? = null,
)

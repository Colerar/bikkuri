package me.hbj.bikkuri.bili.data.live

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.hbj.bikkuri.bili.data.GeneralCode
import me.hbj.bikkuri.bili.data.GeneralCode.UNKNOWN

@Serializable
data class CertificatePacketResponse(
  @SerialName("code") val code: GeneralCode = UNKNOWN,
)

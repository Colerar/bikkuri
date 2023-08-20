package me.hbj.bikkuri.bili.data.info

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.hbj.bikkuri.bili.serializer.data.info.IsOfficialCertifiedSerializer

/**
 * value class 用於封裝
 * @see IsOfficialCertifiedSerializer
 */
@Serializable(with = IsOfficialCertifiedSerializer::class)
@JvmInline
value class IsOfficialCertified(val value: Boolean)

/**
 * 官方认证信息, 可以认为是 [Official] 的迷你版¿
 * @property isCertified 是否认证
 * @property info 简介
 * @see [Official]
 * @see [IsOfficialCertified]
 */
@Serializable
data class OfficialCertify(
  @SerialName("type") val isCertified: IsOfficialCertified = IsOfficialCertified(false),
  @SerialName("desc") val info: String? = null,
)

package me.hbj.bikkuri.bili.data.info

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.hbj.bikkuri.bili.data.GeneralCode
import me.hbj.bikkuri.bili.data.GeneralCode.UNKNOWN

/**
 * 获取基本状态信息 (关注数, 粉丝数, 动态数)
 * @param code 返回值 [GeneralCode]
 * @param message 错误信息
 * @param ttl ttl
 * @param data [StatGetData]
 */
@Serializable
data class StatGetResponse(
  @SerialName("code") val code: GeneralCode = UNKNOWN,
  @SerialName("message") val message: String? = null,
  @SerialName("ttl") val ttl: Int? = null,
  @SerialName("data") val data: StatGetData? = null,
)

/**
 * @param following 关注数
 * @param follower 粉丝数
 * @param dynamicCount 动态数
 */
@Serializable
data class StatGetData(
  @SerialName("following") val following: Int? = null,
  @SerialName("follower") val follower: Int? = null,
  @SerialName("dynamic_count") val dynamicCount: Int? = null,
)

package me.hbj.bikkuri.bili.data.login

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.hbj.bikkuri.bili.data.GeneralCode
import me.hbj.bikkuri.bili.data.GeneralCode.UNKNOWN

/**
 * 获取国际电话区号的响应
 * @property code 状态码
 * @property data [CallingCodeGetResponseData]
 */
@Serializable
data class CallingCodeGetResponse(
  @SerialName("code")
  val code: GeneralCode = UNKNOWN,
  @SerialName("data")
  val data: CallingCodeGetResponseData? = null,
)

/**
 * @param common 常用区号
 * @param others 其他区号
 * @property all 返回全部区号
 * @see [CallingCodeNode]
 */
@Serializable
data class CallingCodeGetResponseData(
  @SerialName("common")
  val common: List<CallingCodeNode> = emptyList(),
  @SerialName("others")
  val others: List<CallingCodeNode> = emptyList(),
) {
  val all: List<CallingCodeNode> = common + others
}

/**
 * @param id ID, 如: 1
 * @param name 名称, 如: 中国大陆
 * @param callingCode 国际区码, 如: 86
 */
@Serializable
data class CallingCodeNode(
  @SerialName("id")
  val id: String? = null,
  @SerialName("cname")
  val name: String? = null,
  @SerialName("country_id")
  val callingCode: String? = null,
)

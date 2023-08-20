package me.hbj.bikkuri.bili.data.live

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.hbj.bikkuri.bili.data.live.LiveResponseCode.UNKNOWN

@Serializable
data class LiveDanmakuInfoGetResponse(
  @SerialName("code") val code: LiveResponseCode = UNKNOWN,
  @SerialName("message") val message: String? = null,
  @SerialName("ttl") val ttl: Int? = null,
  @SerialName("data") val data: LiveDanmakuInfo? = null,
)

@Serializable
data class LiveDanmakuInfo(
  @SerialName("group") val group: String? = null,
  @SerialName("business_id") val businessId: Long? = null,
  @SerialName("refresh_row_factor") val refreshRowFactor: Double? = null,
  @SerialName("refresh_rate") val refreshRate: Double? = null,
  @SerialName("max_delay") val maxDelay: Double? = null,
  @SerialName("token") val token: String? = null,
  @SerialName("host_list") val hostList: List<LiveDanmakuHost> = emptyList(),
)

@Serializable
data class LiveDanmakuHost(
  @SerialName("host") val host: String? = null,
  @SerialName("port") val port: Int? = null,
  @SerialName("wss_port") val wssPort: Int? = null,
  @SerialName("ws_port") val wsPort: Int? = null,
)

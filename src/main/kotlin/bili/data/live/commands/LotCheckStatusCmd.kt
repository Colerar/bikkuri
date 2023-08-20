package me.hbj.bikkuri.bili.data.live.commands

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import me.hbj.bikkuri.bili.data.live.commands.LotStatus.UNKNOWN

@Serializable
data class LotCheckStatusCmd(
  @SerialName("cmd") override val operation: String,
  @SerialName("data") val data: LotStatusData? = null,
) : LiveCommand {
  companion object : LiveCommandFactory() {
    override val operation: String = "ANCHOR_LOT_CHECKSTATUS"
    override fun decode(json: Json, data: JsonElement): LotCheckStatusCmd = json.decodeFromJsonElement(data)
  }
}

@Serializable
data class LotStatusData(
  @SerialName("id") val id: Long? = null,
  @SerialName("reject_reason") val rejectReason: String? = null, // 若 status 爲 REVIEW FAILED 則存在
  @SerialName("status") val status: LotStatus = UNKNOWN,
  @SerialName("uid") val uid: Long? = null, // 主播 Uid
)

package me.hbj.bikkuri.bili.data.live.commands

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement

@Serializable
data class LotEndCmd(
  @SerialName("cmd") override val operation: String,
  @SerialName("data") val data: LotEndData? = null,
) : LiveCommand {
  companion object : LiveCommandFactory() {
    override val operation: String = "ANCHOR_LOT_END"
    override fun decode(json: Json, data: JsonElement): LotEndCmd = json.decodeFromJsonElement(data)
  }
}

@Serializable
data class LotEndData(
  @SerialName("id") val id: Long? = null,
)

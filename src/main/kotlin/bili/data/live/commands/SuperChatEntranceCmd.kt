package me.hbj.bikkuri.bili.data.live.commands

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement

@Serializable
data class SuperChatEntranceCmd(
  @SerialName("cmd") override val operation: String,
  @SerialName("data") val data: SuperChatEntranceData? = null,
) : LiveCommand {
  companion object : LiveCommandFactory() {
    override val operation: String = "SUPER_CHAT_ENTRANCE"

    override fun decode(json: Json, data: JsonElement): SuperChatEntranceCmd = json.decodeFromJsonElement(data)
  }
}

@Serializable
data class SuperChatEntranceData(
  @SerialName("icon") val icon: String? = null,
  @SerialName("jump_url") val jumpUrl: String? = null,
  @SerialName("status") val status: Int? = null,
)

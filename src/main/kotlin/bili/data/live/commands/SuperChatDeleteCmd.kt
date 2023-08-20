package me.hbj.bikkuri.bili.data.live.commands

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement

@Serializable
data class SuperChatDeleteCmd(
  @SerialName("cmd") override val operation: String,
  @SerialName("data") val data: SuperChatDeleteData? = null,
  @SerialName("roomid") val roomId: Long? = null,
) : LiveCommand {
  companion object : LiveCommandFactory() {
    override val operation: String = "SUPER_CHAT_MESSAGE_DELETE"
    override fun decode(json: Json, data: JsonElement): LiveCommand =
      json.decodeFromJsonElement<SuperChatDeleteCmd>(data)
  }
}

@Serializable
data class SuperChatDeleteData(
  @SerialName("ids") val ids: List<Long> = emptyList(),
)

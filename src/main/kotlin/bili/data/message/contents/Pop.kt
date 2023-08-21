package me.hbj.bikkuri.bili.data.message.contents

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.hbj.bikkuri.bili.data.message.MessageType

@Serializable
data class Pop(
  @SerialName("content") val content: List<PopItem> = emptyList(),
) : RecvContent {
  companion object : ContentFactory<Text>() {
    override val code: Int = MessageType.POP.code
    override fun decode(json: Json, data: String): Text = json.decodeFromString(data)
  }
}

@Serializable
data class PopItem(val text: String? = null)

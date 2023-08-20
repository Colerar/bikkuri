package me.hbj.bikkuri.bili.data.message.contents

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.hbj.bikkuri.bili.data.message.MessageType

@Serializable
data class Text(
  @SerialName("content") val content: String? = null,
) : RecvContent {
  companion object : ContentFactory<Text>() {
    override val code: Int = MessageType.TEXT.code
    override fun decode(json: Json, data: String): Text = json.decodeFromString(data)
  }
}

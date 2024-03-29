package me.hbj.bikkuri.bili.data.message

import io.ktor.http.*
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.hbj.bikkuri.bili.Yabapi.defaultJson

@Serializable
data class MessageData(
  val senderUid: Long,
  val receiverId: Long,
  val content: MessageContent,
  val receiverType: Int = 1,
  val devId: String = generateFakeUUID(),
  val timestamp: Long = Clock.System.now().epochSeconds,
)

fun MessageData.put(
  builder: ParametersBuilder,
  json: Json = defaultJson.value,
): ParametersBuilder = builder.apply {
  append("msg[sender_uid]", senderUid.toString())
  append("msg[receiver_id]", receiverId.toString())
  append("msg[receiver_type]", receiverType.toString())
  append("msg[msg_type]", content.code.toString())
  append("msg[dev_id]", devId)
  append("msg[timestamp]", timestamp.toString())
  append("msg[content]", json.encodeToString(content))
}

private const val uuidTemplate = "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx"

private val randomRange by lazy { 0..15 }

private fun generateFakeUUID(): String = uuidTemplate.fold("") { acc, char ->
  acc + when (char) {
    'x' -> randomRange.random().toString(16)
    'y' -> (3 and randomRange.random() or 8).toString(16)
    else -> char
  }
}.uppercase()

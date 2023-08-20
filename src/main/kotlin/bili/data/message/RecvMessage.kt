package me.hbj.bikkuri.bili.data.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.hbj.bikkuri.bili.data.message.contents.ContentFactory
import me.hbj.bikkuri.bili.data.message.contents.RecvContent

private val logger = io.github.oshai.kotlinlogging.KotlinLogging.logger {}

private val json by lazy {
  Json {
    prettyPrint = true
    isLenient = true
    ignoreUnknownKeys = true
    coerceInputValues = true
  }
}

@Serializable
data class RecvMessage(
  @SerialName("sender_uid") val senderUid: Long? = null,
  @SerialName("receiver_type") val receiverType: Int? = null,
  @SerialName("receiver_id") val receiverId: Long? = null,
  @SerialName("msg_type") val msgType: Int? = null,
  @SerialName("content") private val _content: String? = null,
  @SerialName("msg_seqno") val messageSeq: ULong? = null,
  @SerialName("timestamp") val timestamp: ULong? = null,
  @SerialName("at_uids") val atUids: List<Long> = emptyList(),
  @SerialName("msg_key") val key: ULong? = null,
  @SerialName("msg_status") val status: Int? = null,
  @SerialName("notify_code") val notifyCode: String? = null,
  @SerialName("new_face_version") val newFaceVersion: Int? = null,
) {
  val content: RecvContent? by lazy {
    if (_content == null) return@lazy null
    if (msgType == null) return@lazy null
    ContentFactory.map[msgType]?.decode(json, _content) ?: run {
      logger.warn { "Unknown message content type $msgType, raw string: $_content" }
      null
    }
  }
}

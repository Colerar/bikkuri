package me.hbj.bikkuri.bili.data.message

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

enum class MessageType(val code: Int) {
  TEXT(1),
  IMAGE(2),
  RECALL(5),
  POP(18),
}

@Serializable
sealed class MessageContent {

  abstract val code: Int

  @Serializable
  data class Text(
    @SerialName("content") val content: String? = null,
  ) : MessageContent() {
    @Transient override val code: Int = MessageType.TEXT.code
  }

  @Serializable
  data class Image(
    @SerialName("url") val url: String? = null,
  ) : MessageContent() {
    @Transient override val code: Int = MessageType.IMAGE.code
  }

  @Serializable
  data class Recall(
    val key: String,
  ) : MessageContent() {
    @Transient override val code: Int = MessageType.RECALL.code

    companion object : KSerializer<Recall> {
      override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("MessageRecall", PrimitiveKind.STRING)
      override fun serialize(encoder: Encoder, value: Recall): Unit = encoder.encodeString(value.key)
      override fun deserialize(decoder: Decoder): Recall = Recall(decoder.decodeString())
    }
  }
}

package me.hbj.bikkuri.bili.serializer.data

import io.ktor.util.date.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object GMTDateSerializer : KSerializer<GMTDate> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("GMTDateSerializer", PrimitiveKind.LONG)

  override fun serialize(encoder: Encoder, value: GMTDate): Unit = encoder.encodeLong(value.timestamp)

  override fun deserialize(decoder: Decoder): GMTDate = GMTDate(decoder.decodeLong())
}

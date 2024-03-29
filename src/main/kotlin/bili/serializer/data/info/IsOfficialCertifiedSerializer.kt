package me.hbj.bikkuri.bili.serializer.data.info

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import me.hbj.bikkuri.bili.data.info.IsOfficialCertified
import me.hbj.bikkuri.bili.exception.UnsupportedDecoderException

/**
 * 为 0 时返回 true, 其余为 false
 */
object IsOfficialCertifiedSerializer : KSerializer<IsOfficialCertified> {

  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("IsOffcialCertifedSerializer", PrimitiveKind.BOOLEAN)

  override fun serialize(encoder: Encoder, value: IsOfficialCertified): Unit = when (value.value) {
    true -> encoder.encodeInt(0)
    false -> encoder.encodeInt(-1)
  }

  override fun deserialize(decoder: Decoder): IsOfficialCertified = when (decoder) {
    is JsonDecoder -> IsOfficialCertified(decoder.decodeInt() == 0)
    else -> throw UnsupportedDecoderException(decoder)
  }
}

package me.hbj.bikkuri.bili.serializer.data.info

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import me.hbj.bikkuri.bili.data.info.OfficialRole
import me.hbj.bikkuri.bili.data.info.OfficialRole.UNKNOWN
import me.hbj.bikkuri.bili.exception.UnsupportedDecoderException

/**
 * 根据 [OfficialRole.codes] 序列化
 */
object OfficialRoleSerializer : KSerializer<OfficialRole> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("OffcialRoleSerializer", PrimitiveKind.INT)

  override fun serialize(encoder: Encoder, value: OfficialRole): Unit =
    encoder.encodeInt(value.codes.firstOrNull() ?: 0)

  override fun deserialize(decoder: Decoder): OfficialRole = when (decoder) {
    is JsonDecoder -> run {
      val decoded = decoder.decodeInt()
      enumValues<OfficialRole>().firstOrNull { it.codes.contains(decoded) } ?: UNKNOWN
    }
    else -> throw UnsupportedDecoderException(decoder)
  }
}

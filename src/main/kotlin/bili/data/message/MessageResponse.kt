@file:UseSerializers(BooleanJsSerializer::class)

package me.hbj.bikkuri.bili.data.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import me.hbj.bikkuri.bili.data.GeneralCode
import me.hbj.bikkuri.bili.data.GeneralCode.UNKNOWN
import me.hbj.bikkuri.bili.serializer.BooleanJsSerializer

@Serializable
data class MessageResponse(
  @SerialName("code") val code: GeneralCode = UNKNOWN,
  @SerialName("msg") val msg: String? = null,
  @SerialName("message") val message: String? = null,
  @SerialName("ttl") val ttl: Int? = null,
  @SerialName("data") val data: Data? = null,
) {
  @Serializable
  data class Data(
    @SerialName("messages") val messages: List<RecvMessage> = emptyList(),
    @SerialName("has_more") val hasMore: Boolean? = null,
    @SerialName("min_seqno") val minSeq: ULong? = null,
    @SerialName("max_seqno") val maxSeq: ULong? = null,
    @SerialName("e_infos") val emoticons: List<Emoticon>? = null,
  )

  @Serializable
  data class Emoticon(
    @SerialName("text") val text: String? = null,
    @SerialName("url") val url: String? = null,
    @SerialName("size") val size: Int? = null,
  )
}

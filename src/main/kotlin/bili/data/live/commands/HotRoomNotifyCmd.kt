@file:UseSerializers(BooleanJsSerializer::class)

package me.hbj.bikkuri.bili.data.live.commands

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import me.hbj.bikkuri.bili.serializer.BooleanJsSerializer

@Serializable
data class HotRoomNotifyCmd(
  @SerialName("cmd") override val operation: String,
  @SerialName("data") val data: HotRoomNotify? = null,
) : LiveCommand {
  companion object : LiveCommandFactory() {
    override val operation: String = "HOT_ROOM_NOTIFY"
    override fun decode(json: Json, data: JsonElement): HotRoomNotifyCmd = json.decodeFromJsonElement(data)
  }
}

@Serializable
data class HotRoomNotify(
  @SerialName("threshold") val threshold: Int? = null,
  @SerialName("ttl") val ttl: Int? = null,
  @SerialName("exit_no_refresh") val exitNoRefresh: Boolean? = null,
  @SerialName("random_delay_req_v2") val randomDelayReqV2: List<RequestNode> = emptyList(),
) {
  @Serializable
  data class RequestNode(
    @SerialName("path") val path: String? = null,
    @SerialName("delay") val delay: Int? = null,
  )
}

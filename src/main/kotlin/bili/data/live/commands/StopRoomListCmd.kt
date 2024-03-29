package me.hbj.bikkuri.bili.data.live.commands

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement

@Serializable
data class StopRoomListCmd(
  @SerialName("cmd") override val operation: String,
  @SerialName("data") val data: StopLiveRoomData? = null,
) : LiveCommand {
  inline val list: List<Int> // shortcut for `data.list`
    get() = data?.list ?: emptyList()

  companion object : LiveCommandFactory() {
    override val operation: String = "STOP_LIVE_ROOM_LIST"
    override fun decode(json: Json, data: JsonElement): StopRoomListCmd = json.decodeFromJsonElement(data)
  }
}

@Serializable
data class StopLiveRoomData(
  @SerialName("room_id_list") val list: List<Int> = emptyList(),
)

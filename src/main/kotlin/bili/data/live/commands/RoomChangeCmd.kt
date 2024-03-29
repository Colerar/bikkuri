package me.hbj.bikkuri.bili.data.live.commands

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement

@Serializable
data class RoomChangeCmd(
  @SerialName("cmd") override val operation: String,
  @SerialName("data") val data: RoomChangeData? = null,
) : LiveCommand {
  companion object : LiveCommandFactory() {
    override val operation: String = "ROOM_CHANGE"
    override fun decode(json: Json, data: JsonElement): RoomChangeCmd = json.decodeFromJsonElement(data)
  }
}

@Serializable
data class RoomChangeData(
  @SerialName("title") val title: String? = null,
  @SerialName("area_id") val areaId: Long? = null,
  @SerialName("parent_area_id") val parentAreaId: Long? = null,
  @SerialName("area_name") val areaName: String? = null,
  @SerialName("parent_area_name") val parentAreaName: String? = null,
  @SerialName("live_key") val liveKey: String? = null,
  @SerialName("sub_session_key") val subSessionKey: String? = null,
)

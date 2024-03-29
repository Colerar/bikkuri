@file:UseSerializers(BooleanJsSerializer::class)

package me.hbj.bikkuri.bili.data.live.commands

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import me.hbj.bikkuri.bili.data.RgbColor
import me.hbj.bikkuri.bili.serializer.BooleanJsSerializer
import me.hbj.bikkuri.bili.serializer.data.RgbColorStringSerializerNullable

@Serializable
data class EntryEffectCmd(
  @SerialName("cmd") override val operation: String,
  @SerialName("data") val data: EntryEffectData? = null,
) : LiveCommand {
  companion object : LiveCommandFactory() {
    override val operation: String = "ENTRY_EFFECT"
    override fun decode(json: Json, data: JsonElement): EntryEffectCmd = json.decodeFromJsonElement(data)
  }
}

@Serializable
data class EntryEffectData(
  @SerialName("id") val id: Long? = null,
  @SerialName("uid") val uid: Long? = null,
  @SerialName("target_id") val targetId: Long? = null,
  @SerialName("mock_effect") val mockEffect: Int? = null,
  @SerialName("face") val avatar: String? = null,
  @SerialName("privilege_type") val privilegeType: Int? = null,
  @SerialName("copy_writing") val copyWriting: String? = null,
  @Serializable(RgbColorStringSerializerNullable::class)
  @SerialName("copy_color") val copyColor: RgbColor? = null,
  @Serializable(RgbColorStringSerializerNullable::class)
  @SerialName("highlight_color") val highlightColor: RgbColor? = null,
  @SerialName("priority") val priority: Int? = null,
  @SerialName("basemap_url") val basemapUrl: String? = null,
  @SerialName("show_avatar") val showAvatar: Boolean? = null,
  @SerialName("effective_time") val effectiveTime: Int? = null,
  @SerialName("effective_time_new") val effectiveTimeNew: Int? = null,
  @SerialName("web_basemap_url") val webBasemapUrl: String? = null,
  @SerialName("web_effective_time") val webEffectiveTime: Int? = null,
  @SerialName("web_effect_close") val webEffectClose: Int? = null,
  @SerialName("web_close_time") val webCloseTime: Int? = null,
  @SerialName("business") val business: Int? = null,
  @SerialName("copy_writing_v2") val copyWritingV2: String? = null, // 欢迎 <^icon^> 提督 <%username%> 进入直播间",
  @SerialName("icon_list") val iconList: List<Int> = emptyList(),
  @SerialName("max_delay_time") val maxDelayTime: Int? = null,
  @SerialName("trigger_time") val triggerTime: Long? = null,
  @SerialName("identities") val identities: Int? = null,
  @SerialName("effect_silent_time") val effectSilentTime: Int? = null,
)

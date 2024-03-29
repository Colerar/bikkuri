@file:UseSerializers(BooleanJsSerializer::class)

package me.hbj.bikkuri.bili.data.live.commands

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import me.hbj.bikkuri.bili.data.live.commands.LotStatus.UNKNOWN
import me.hbj.bikkuri.bili.serializer.BooleanJsSerializer

@Serializable
data class LotStartCmd(
  @SerialName("cmd") override val operation: String,
  @SerialName("data") val data: LotStartData? = null,
) : LiveCommand {
  companion object : LiveCommandFactory() {
    override val operation: String = "ANCHOR_LOT_START"
    override fun decode(json: Json, data: JsonElement): LotStartCmd = json.decodeFromJsonElement(data)
  }
}

@Serializable
data class LotStartData(
  @SerialName("asset_icon") val assetIcon: String? = null,
  @SerialName("award_image") val awardImage: String? = null,
  @SerialName("award_name") val awardName: String? = null,
  @SerialName("award_num") val awardNum: Int? = null, // 總數
  @SerialName("cur_gift_num") val curGiftNum: Int? = null, // 未知
  @SerialName("current_time") val currentTime: Long? = null,
  @SerialName("danmu") val danmu: String? = null,
  @SerialName("gift_id") val giftId: Long? = null,
  @SerialName("gift_name") val giftName: String? = null,
  @SerialName("gift_num") val giftNum: Int? = null, // 每人的數量
  @SerialName("gift_price") val giftPrice: Int? = null,
  @SerialName("goaway_time") val goawayTime: Int? = null,
  @SerialName("goods_id") val goodsId: Long? = null,
  @SerialName("id") val id: Long? = null,
  @SerialName("is_broadcast") val isBroadcast: Boolean? = null,
  @SerialName("join_type") val joinType: Int? = null,
  @SerialName("lot_status") val lotStatus: LotStatus = UNKNOWN,
  @SerialName("max_time") val maxTime: Int? = null,
  @SerialName("require_text") val requireText: String? = null,
  @SerialName("require_value") val requireValue: String? = null,
  @SerialName("room_id") val roomId: Long? = null,
  @SerialName("send_gift_ensure") val sendGiftEnsure: Boolean? = null, // 確認界面
  @SerialName("show_panel") val showPanel: Boolean? = null,
  @SerialName("status") val status: Boolean? = null,
  @SerialName("time") val time: Int? = null,
  @SerialName("url") val url: String? = null,
  @SerialName("web_url") val webUrl: String? = null,
)

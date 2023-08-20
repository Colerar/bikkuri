package me.hbj.bikkuri.bili.data.live.commands

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.*
import me.hbj.bikkuri.bili.Yabapi.defaultJson

@Serializable
data class RawLiveCommand(
  val value: JsonObject,
) {
  val operation: String by lazy {
    value.jsonObject["cmd"]?.jsonPrimitive?.content
      ?: throw SerializationException("Required [cmd] field cannot find in JsonObject $value")
  }

  val data: LiveCommand? by lazy {
    LiveCommandFactory.map[operation]?.decode(defaultJson.value, value)
  }
}

sealed interface LiveCommand {
  val operation: String
}

sealed interface LiveCommandData

sealed class LiveCommandFactory {

  abstract val operation: String

  abstract fun decode(json: Json, data: JsonElement): LiveCommand

  companion object {
    private val factories: List<LiveCommandFactory> =
      listOf(
        ComboSendCmd,
        DanmakuMsgCmd,
        EntryEffectCmd,
        GuardBuyCmd,
        HotRankChangeCmd,
        HotRankChangeV2Cmd,
        HotRankSettlementCmd,
        HotRankSettlementV2Cmd,
        HotRoomNotifyCmd,
        InteractWordCmd,
        LikeInfoV3UpdateCmd,
        LiveInteractGameCmd,
        LotAwardCmd,
        LotCheckStatusCmd,
        LotEndCmd,
        LotStartCmd,
        MatchRoomConfCmd,
        NoticeMsgCmd,
        OnlineRankCountCmd,
        OnlineRankTopCmd,
        OnlineRankV2Cmd,
        RoomChangeCmd,
        RoomUpdateCmd,
        SendGiftCmd,
        StopRoomListCmd,
        SuperChatDeleteCmd,
        SuperChatEntranceCmd,
        SuperChatMsgCmd,
        SuperChatMsgJpnCmd,
        UserToastMsgCmd,
        WatchedChangeCmd,
        WidgetBannerCmd,
      )

    val map: Map<String, LiveCommandFactory> =
      factories.associateBy { it.operation }
  }
}

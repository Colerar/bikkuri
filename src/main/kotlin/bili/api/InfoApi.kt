package me.hbj.bikkuri.bili.api

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.withContext
import me.hbj.bikkuri.bili.BiliClient
import me.hbj.bikkuri.bili.consts.internal.*
import me.hbj.bikkuri.bili.data.info.*
import me.hbj.bikkuri.bili.data.space.*
import me.hbj.bikkuri.bili.deserializeJson
import me.hbj.bikkuri.bili.wbiMixinKey
import kotlin.coroutines.CoroutineContext

private val logger = io.github.oshai.kotlinlogging.KotlinLogging.logger {}

// region ============= Self =================

/**
 * 获取自己的基本信息, 需登录
 * @see [BasicInfoGetResponse]
 */
suspend fun BiliClient.getBasicInfo(
  context: CoroutineContext = this.context,
): BasicInfoGetResponse = withContext(context) {
  logger.debug { "Getting basic info..." }
  client.get(BASIC_INFO_GET_URL).also {
    logger.debug { "Basic info response: $it" }
  }.body<String>().deserializeJson<BasicInfoGetResponse>()
    .also {
      val biliClient = this@getBasicInfo
      wbiMixinKey = biliClient.getMixinKey(
        Url(it.data.wbi.img).pathSegments.last().split('.').first(),
        Url(it.data.wbi.sub).pathSegments.last().split('.').first(),
      )
      logger.debug { "Updated wbiMixinKey to `$wbiMixinKey`" }
    }
}

/**
 * 获取自己的状态值, 需登录
 * @see StatGetResponse
 */
suspend fun BiliClient.getStat(
  context: CoroutineContext = this.context,
): StatGetResponse = withContext(context) {
  logger.debug { "Getting stat info..." }
  client.get(STAT_GET_URL)
    .body<String>()
    .deserializeJson<StatGetResponse>()
    .also { logger.debug { "Got stat info response: $it" } }
}

/**
 * 获取自己的硬币状态数
 * @see CoinGetResponse
 */
suspend fun BiliClient.getCoinInfo(
  context: CoroutineContext = this.context,
): CoinGetResponse = withContext(context) {
  logger.debug { "Getting coin number..." }
  client.get(COIN_GET_URL)
    .body<String>()
    .deserializeJson<CoinGetResponse>()
    .also { logger.debug { "Got Coin info response: $it" } }
}

/**
 * 获取自己的帐号信息, 个人简介相关
 * @see AccountInfoGetResponse
 */
suspend fun BiliClient.getAccountInfo(
  context: CoroutineContext = this.context,
): AccountInfoGetResponse = withContext(context) {
  logger.debug { "Getting Account Info..." }
  client.get(ACCOUNT_INFO_GET_URL)
    .body<String>()
    .deserializeJson<AccountInfoGetResponse>()
    .also { logger.debug { "Got Account Info Response: $it" } }
}

/**
 * 获取自身经验计算相关属性
 * @see ExpRewardGetResponse
 */
suspend fun BiliClient.getExpReward(
  context: CoroutineContext = this.context,
): ExpRewardGetResponse = withContext(context) {
  logger.debug { "Getting Exp Reward..." }
  client.get(EXP_REWARD_GET_URL)
    .body<String>()
    .deserializeJson<ExpRewardGetResponse>()
    .also { logger.debug { "Got Exp Reward Response: $it" } }
}

/**
 * 获取硬币经验, 与 [getExpReward] 不同的是, 此接口实时更新
 * @see CoinExpGetResponse
 */
suspend fun BiliClient.getCoinExp(
  context: CoroutineContext = this.context,
): CoinExpGetResponse = withContext(context) {
  logger.debug { "Getting Coin Exp..." }
  client.get(COIN_EXP_GET_URL)
    .body<String>()
    .deserializeJson<CoinExpGetResponse>()
    .also { logger.debug { "Got Coin Exp Response: $it" } }
}

/**
 * 获取自身 Vip 状态值
 * @see VipStatGetResponse
 */
suspend fun BiliClient.getVipStat(
  context: CoroutineContext = this.context,
): VipStatGetResponse = withContext(context) {
  logger.debug { "Getting Vip Stat..." }
  client.get(VIP_STAT_GET_URL)
    .body<String>()
    .deserializeJson<VipStatGetResponse>()
    .also { logger.debug { "Got Vip Stat Response: $it" } }
}

/**
 * 获取自身安全状态信息
 * @see SecureInfoGetResponse
 */
suspend fun BiliClient.getSecureInfo(
  context: CoroutineContext = this.context,
): SecureInfoGetResponse = withContext(context) {
  logger.debug { "Getting Secure Info..." }
  client.get(SECURE_INFO_GET_URL)
    .body<String>()
    .deserializeJson<SecureInfoGetResponse>()
    .also { logger.debug { "Got Secure Info: $it" } }
}

/**
 * 获取自身实名信息
 * @see RealNameInfoGetResponse
 */
suspend fun BiliClient.getRealNameInfo(
  context: CoroutineContext = this.context,
): RealNameInfoGetResponse = withContext(context) {
  logger.debug { "Getting Real Name Info..." }
  client.get(REAL_NAME_INFO_GET_URL)
    .body<String>()
    .deserializeJson<RealNameInfoGetResponse>()
    .also { logger.debug { "Got Real Name Info: $it" } }
}

/**
 * 获取详细实名信息
 * @see RealNameDetailedGetResponse
 */
suspend fun BiliClient.getRealNameDetailed(
  context: CoroutineContext = this.context,
): RealNameDetailedGetResponse =
  withContext(context) {
    logger.debug { "Getting Real Name Detailed..." }
    client.get(REAL_NAME_DETAILED_GET_URL)
      .body<String>()
      .deserializeJson<RealNameDetailedGetResponse>()
      .also { logger.debug { "Got Real Name Detailed: $it" } }
  }

/**
 * 获取硬币收支记录
 * @see CoinLogGetResponse
 */
suspend fun BiliClient.getCoinLog(
  context: CoroutineContext = this.context,
): CoinLogGetResponse = withContext(context) {
  logger.debug { "Getting Coin Log..." }
  client.get(COIN_LOG_GET_URL)
    .body<String>()
    .deserializeJson<CoinLogGetResponse>()
    .also { logger.debug { "Got Coin Log: $it" } }
}

/**
 * 获取自身用户空间信息
 */
suspend fun BiliClient.getMySpace(
  context: CoroutineContext = this.context,
): MySpaceGetResponse = withContext(context) {
  logger.debug { "Getting Current User Space Info:" }
  client.get(MY_SPACE_GET_URL)
    .body<String>()
    .deserializeJson<MySpaceGetResponse>()
    .also { logger.debug { "Got Current User Space Info: $it" } }
}

// endregion

// region ============= Target =================

/**
 * 获取目标用户空间信息
 * @param mid 用户 mid
 * @see UserSpaceGetResponse
 * @see getMySpace
 */
suspend fun BiliClient.getUserSpace(
  mid: Long,
  context: CoroutineContext = this.context,
): UserSpaceGetResponse = withContext(context) {
  logger.debug { "Getting User Space Info..." }
  client.get(USER_SPACE_GET_WBI_URL) {
    parameter("mid", mid.toString())
  }.body<String>().deserializeJson<UserSpaceGetResponse>().also {
    logger.debug { "Got User $mid Space Info: $it" }
  }
}

/**
 * 获取用户卡片信息
 * @param mid 目标用户 mid
 * @param requestBanner 是否请求空间头图 banner
 * @see UserCardGetResponse
 */
suspend fun BiliClient.getUserCard(
  mid: Long,
  requestBanner: Boolean,
  context: CoroutineContext = this.context,
): UserCardGetResponse = withContext(context) {
  logger.debug { "Getting User Card Info..." }
  client.get(USER_CARD_GET_URL) {
    parameter("mid", mid.toString())
    parameter("photo", requestBanner.toString())
  }.body<String>().deserializeJson<UserCardGetResponse>().also {
    logger.debug { "Got User $mid Card Info: $it" }
  }
}

/**
 * 获取用户 tags
 * @param mid 目标用户mid
 * @return [UserTagsGetResponse]
 */
suspend fun BiliClient.getUserTags(
  mid: Long,
  context: CoroutineContext = this.context,
): UserTagsGetResponse = withContext(context) {
  logger.debug { "Getting user tags for mid$mid..." }
  client.get(USER_TAGS_GET_URL) {
    parameter("mid", mid)
  }.body<String>().deserializeJson<UserTagsGetResponse>().also {
    logger.debug { "Got user tags for mid $mid: $it" }
  }
}

/**
 * 获取用户空间公告
 * @param mid 目标用户 mid
 */
suspend fun BiliClient.getSpaceAnnouncement(
  mid: Long,
  context: CoroutineContext = this.context,
): SpaceAnnouncementGetResponse = withContext(context) {
  logger.debug { "Getting Space Announcement for mid$mid..." }
  client.get(USER_SPACE_ANNOUNCEMENT_GET_URL) {
    parameter("mid", mid)
  }.body<String>().deserializeJson<SpaceAnnouncementGetResponse>().also {
    logger.debug { "Got Space Announcent for mid$mid: $it" }
  }
}

/**
 * 获取目标用户空间设置
 * @param mid 目标用户mid
 */
suspend fun BiliClient.getSpaceSetting(
  mid: Long,
  context: CoroutineContext = this.context,
): SpaceSettingResponse = withContext(context) {
  logger.debug { "Getting Space Setting for mid$mid..." }
  client.get(USER_SPACE_SETTING_GET_URL) {
    parameter("mid", mid)
  }.body<String>().deserializeJson<SpaceSettingResponse>().also {
    logger.debug { "Got Space Setting for mid$mid: $it" }
  }
}

/**
 * 獲取目標自己創建的收藏夾
 */
suspend fun BiliClient.getFavorites(
  targetMid: Long,
  context: CoroutineContext = this.context,
): FavoritesGetResponse = withContext(context) {
  logger.debug { "Getting Favorites List for mid $targetMid..." }
  client.get(SPACE_COLLECTION_LIST_GET_URL) {
    parameter("up_mid", targetMid)
  }.body<String>().deserializeJson<FavoritesGetResponse>().also {
    logger.debug { "Got Favorites List for mid $targetMid" }
  }
}

/**
 * 目標獲取收藏他人的收藏夾
 * Fav means Favorite
 * @param targetMid 目標 mid
 * @param page 頁碼
 * @param pageSize 單頁大小
 */
suspend fun BiliClient.getCollectedFavorites(
  targetMid: Long,
  page: Int = 1,
  pageSize: Int = 20,
  platform: String = "web",
  context: CoroutineContext = this.context,
): CollectedFavoritesGetResponse = withContext(context) {
  logger.debug { "Getting Collected Favorites List for mid $targetMid..." }
  client.get(SPACE_FAV_COLLECTION_LIST_GET_URL) {
    parameter("up_mid", targetMid)
    parameter("pn", page)
    parameter("ps", pageSize)
    parameter("platform", platform)
  }.body<String>().deserializeJson<CollectedFavoritesGetResponse>().also {
    logger.debug { "Got Collected Favorites List for mid $targetMid" }
  }
}

suspend fun BiliClient.getSubscribedTags(
  targetMid: Long,
  context: CoroutineContext = this.context,
): SubscribedTagsResponse = withContext(context) {
  logger.debug { "Getting Subscribed Tags for $targetMid" }
  client.get(SPACE_SUB_TAGS_GET_URL) {
    parameter("mid", targetMid)
  }.body<String>().deserializeJson<SubscribedTagsResponse>().also {
    logger.debug { "Got Subscribed Tags for $targetMid" }
  }
}

// endregion

// region ============= Nick =================

/**
 * 检查名称是否可用
 * @param nick 需要检查的昵称
 * @see CheckNickResponse
 */
suspend fun BiliClient.checkNick(
  nick: String,
  context: CoroutineContext = this.context,
): CheckNickResponse = withContext(context) {
  logger.debug { "Checking Nick Status..." }
  client.get(NICK_CHECK_URL) {
    parameter("nickName", nick)
  }.body<String>().deserializeJson<CheckNickResponse>().also {
    logger.debug { "Nick \"$nick\" status: $it" }
  }
}

// endregion

// region ============= Favorites =================

/**
 * 根据 id 获得特定收藏夹的信息
 */
suspend fun BiliClient.getFavoritesInfo(
  id: Long,
  context: CoroutineContext = this.context,
): FavoritesInfoResponse = withContext(context) {
  logger.debug { "Getting favorites info " }
  client.get(FAVORITES_INFO_GET_URL) {
    parameter("media_id", id)
  }.body<String>().deserializeJson<FavoritesInfoResponse>().also {
    logger.debug { "Got favorites info: $it" }
  }
}

suspend fun BiliClient.getFavoritesTypes(
  mid: Long,
  favoritesId: Long,
  context: CoroutineContext = this.context,
): FavoritesTypeResponse = withContext(context) {
  logger.debug { "Getting Favorites Type for $favoritesId($mid)" }
  client.get(FAVORITES_TYPES_GET_URL) {
    parameter("up_mid", mid)
    parameter("media_id", favoritesId)
  }.body<String>().deserializeJson<FavoritesTypeResponse>().also {
    logger.debug { "Got Favorites Type for $favoritesId($mid): $it" }
  }
}

// endregion

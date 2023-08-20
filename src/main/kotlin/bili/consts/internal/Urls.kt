package me.hbj.bikkuri.bili.consts.internal

// region =================== Base ========================

internal const val WWW: String = "https://www.bilibili.com"

/** 主站 */
internal const val MAIN: String = "https://api.bilibili.com"

/** SPACE */
internal const val SPACE: String = "https://space.bilibili.com"

/** 直播中心 */
internal const val LIVE: String = "https://api.live.bilibili.com"

/** 鉴权 */
internal const val PASSPORT: String = "https://passport.bilibili.com"

internal const val ACCOUNT: String = "https://account.bilibili.com"

internal const val VC_API: String = "https://api.vc.bilibili.com"

// endregion

// region =================== Info ========================

internal const val BASIC_INFO_GET_URL = "$MAIN/x/web-interface/nav"

internal const val STAT_GET_URL = "$MAIN/x/web-interface/nav/stat"

internal const val COIN_GET_URL = "$ACCOUNT/site/getCoin"

internal const val USER_CARD_GET_URL = "$MAIN/x/web-interface/card"

internal const val NICK_CHECK_URL = "$PASSPORT/web/generic/check/nickname"

// region ## =================== Space ========================

internal const val MY_SPACE_GET_URL = "$MAIN/x/space/myinfo"

internal const val USER_SPACE_GET_URL = "$MAIN/x/space/acc/info"

internal const val USER_TAGS_GET_URL = "$MAIN/x/space/acc/tags"

internal const val USER_SPACE_ANNOUNCEMENT_GET_URL = "$MAIN/x/space/notice"

internal const val USER_SPACE_SETTING_GET_URL = "$SPACE/ajax/settings/getSettings"

internal const val SPACE_COLLECTION_LIST_GET_URL = "$MAIN/x/v3/fav/folder/created/list-all"

internal const val SPACE_FAV_COLLECTION_LIST_GET_URL = "$MAIN/x/v3/fav/folder/collected/list"

internal const val SPACE_SUB_TAGS_GET_URL = "$SPACE/ajax/tags/getSubList"

// endregion

// region ## =================== Personal Centre ========================

internal const val ACCOUNT_INFO_GET_URL: String = "$MAIN/x/member/web/account"

internal const val EXP_REWARD_GET_URL: String = "$MAIN/x/member/web/exp/reward"

internal const val COIN_EXP_GET_URL: String = "$WWW/plus/account/exp.php"

internal const val VIP_STAT_GET_URL: String = "$MAIN/x/vip/web/user/info"

internal const val SECURE_INFO_GET_URL: String = "$PASSPORT/web/site/user/info"

internal const val REAL_NAME_INFO_GET_URL: String = "$MAIN/x/member/realname/status"

internal const val REAL_NAME_DETAILED_GET_URL: String = "$MAIN/x/member/realname/apply/status"

internal const val COIN_LOG_GET_URL: String = "$MAIN/x/member/web/coin/log"

// endregion

// region ## =================== Fav Folder ========================

internal const val FAVORITES_INFO_GET_URL = "$MAIN/x/v3/fav/folder/info"

internal const val FAVORITES_TYPES_GET_URL = "$MAIN/x/v3/fav/resource/partition"

// endregion

// endregion

// region =================== Live ========================

internal const val LIVE_INIT_INFO_GET_URL = "$LIVE/room/v1/Room/room_init"

internal const val LIVE_ROOM_INFO_URL = "$LIVE/room/v1/Room/get_info"

internal const val LIVE_AREA_URL = "$LIVE/room/v1/Area/getList"

internal const val LIVE_UID_TO_ROOM_ID = "$LIVE/room/v2/Room/room_id_by_uid"

internal const val LIVER_INFO_GET_URL = "$LIVE/live_user/v1/Master/info"

internal const val LIVE_SHOW_LIST_GET = "$LIVE/room/v1/Index/getShowList"

internal const val LIVE_CHECK_PWD_URL = "$LIVE/room/v1/Room/verify_room_pwd"

internal const val LIVE_HOVER_GET_URL = "$LIVE/room/v2/Index/getHoverInfo"

internal const val LIVE_DANMAKU_INFO_URL = "$LIVE/xlive/web-room/v1/index/getDanmuInfo"

internal const val LIVE_SIGN_URL = "$LIVE/xlive/web-ucenter/v1/sign/DoSign"

internal const val LIVE_SIGN_INFO_URL = "$LIVE/xlive/web-ucenter/v1/sign/WebGetSignInfo"

internal const val LIVE_SIGN_LAST_MONTH_URL = "$LIVE/sign/getLastMonthSignDays"

internal const val LIVE_RANKING_GET_URL = "$LIVE/rankdb/v1/Rank2018/getWebTop"

internal const val LIVE_MEDAL_RANK_GET_URL = "$LIVE/xlive/general-interface/v1/Rank/GetTotalMedalLevelRank"

internal const val LIVE_GUARD_LIST_GET_URL = "$LIVE/xlive/app-room/v2/guardTab/topList"

// endregion

// region =================== Passport ========================

internal const val QUERY_CAPTCHA_URL: String = "$PASSPORT/x/passport-login/captcha"

internal const val RSA_GET_WEB_URL: String = "$PASSPORT/login"

internal const val LOGIN_WEB_URL: String = "$PASSPORT/x/passport-login/web/login"

internal const val LOGIN_QRCODE_GET_WEB_URL: String = "$PASSPORT/qrcode/getLoginUrl"

internal const val LOGIN_WEB_QRCODE_URL = "$PASSPORT/qrcode/getLoginInfo"

internal const val GET_CALLING_CODE_URL: String = "$PASSPORT/web/generic/country/list"

internal const val SEND_SMS_URL: String = "$PASSPORT/x/passport-login/web/sms/send"

internal const val LOGIN_WEB_SMS_URL: String = "$PASSPORT/x/passport-login/web/login/sms"

internal const val LOG_OUT_URL = "$PASSPORT/login/exit/v2"

// endregion

// region =================== Message ========================

internal const val UNREAD_MESSAGE_COUNT_GET_URL = "$MAIN/x/msgfeed/unread"

internal const val UNREAD_WHISPER_COUNT_GET_URL = "$VC_API/session_svr/v1/session_svr/single_unread"

internal const val SEND_MESSAGE_URL = "$VC_API/web_im/v1/web_im/send_msg"

internal const val FETCH_MESSAGE_SESSIONS_URL = "$VC_API/session_svr/v1/session_svr/get_sessions"

internal const val FETCH_NEW_MESSAGE_SESSIONS_URL = "$VC_API/session_svr/v1/session_svr/new_sessions"

internal const val MESSAGE_SETTINGS_URL = "$VC_API/link_setting/v1/link_setting/set"

internal const val FETCH_SESSION_MESSAGES_URL = "$VC_API/svr_sync/v1/svr_sync/fetch_session_msgs"

// endregion

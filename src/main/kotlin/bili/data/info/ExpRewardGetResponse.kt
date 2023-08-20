package me.hbj.bikkuri.bili.data.info

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.hbj.bikkuri.bili.data.GeneralCode
import me.hbj.bikkuri.bili.data.GeneralCode.UNKNOWN

/**
 * @param code 返回值 [GeneralCode]
 * @param data [ExpReward]
 */
@Serializable
data class ExpRewardGetResponse(
  @SerialName("code") val code: GeneralCode = UNKNOWN,
  @SerialName("message") val message: String? = null,
  @SerialName("ttl") val ttl: Int? = null,
  @SerialName("data") val data: ExpReward? = null,
)

private const val LOGIN_EXP = 5
private const val WATCH_EXP = 5
private const val SHARE_EXP = 5

@Serializable
data class ExpReward(
  @SerialName("login") val isLogin: Boolean? = null,
  @SerialName("watch") val isWatched: Boolean? = null,
  @SerialName("coins") val coinExp: Int? = null,
  @SerialName("share") val isShared: Boolean? = null,
  @SerialName("email") val isEmailVerified: Boolean? = null,
  @SerialName("tel") val isPhoneVerified: Boolean? = null,
  @SerialName("safe_question") val hasSafeQuestion: Boolean? = null,
  @SerialName("identify_card") val isIdCardVerified: Boolean? = null,
) {
  fun countTodayRewarded(): Int {
    var rewarded = 0
    if (isLogin == true) rewarded += LOGIN_EXP
    if (isWatched == true) rewarded += WATCH_EXP
    if (isShared == true) rewarded += SHARE_EXP
    coinExp?.let { rewarded += it }
    return rewarded
  }

  fun replaceWithCoinExp(data: CoinExpGetResponse): ExpReward {
    requireNotNull(data.coinExp)
    return this.copy(coinExp = data.coinExp)
  }
}

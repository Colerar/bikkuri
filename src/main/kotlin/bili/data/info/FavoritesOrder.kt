package me.hbj.bikkuri.bili.data.info

import kotlinx.serialization.Serializable
import me.hbj.bikkuri.bili.data.info.FavoritesOrder.*

/**
 * @property FAVORITE_TIME 收藏时间 新 -> 旧
 * @property RELEASE_TIME 发布时间 新 -> 旧
 * @property VIEW 播放降序
 */
@Serializable
enum class FavoritesOrder(val code: String) {
  FAVORITE_TIME("mtime"),

  RELEASE_TIME("pubtime"),

  VIEW("view"),
}

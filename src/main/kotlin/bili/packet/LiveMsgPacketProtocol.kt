package me.hbj.bikkuri.bili.packet

import kotlinx.serialization.Serializable
import me.hbj.bikkuri.bili.packet.LiveMsgPacketProtocol.*

/**
 * @property COMMAND_NO_COMPRESSION 普通包正文無壓縮
 * @property SPECIAL_NO_COMPRESSION 心跳/認證正文無壓縮
 * @property COMMAND_ZLIB 普通包正文使用 ZLIB 壓縮
 * @property COMMAND_BROTLI 普通包正文使用 BROTLI 壓縮, 解壓爲帶頭部普通包
 */
@Serializable
enum class LiveMsgPacketProtocol(val code: UShort) {
  COMMAND_NO_COMPRESSION(0u),

  SPECIAL_NO_COMPRESSION(1u),

  COMMAND_ZLIB(2u),

  COMMAND_BROTLI(3u),
  ;

  companion object {
    fun fromCode(code: UShort): LiveMsgPacketProtocol? = values().firstOrNull { code == it.code }
  }
}

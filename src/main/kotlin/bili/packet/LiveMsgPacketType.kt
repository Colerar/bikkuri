package me.hbj.bikkuri.bili.packet

enum class LiveMsgPacketType(val code: UInt) {
  HEARTBEAT(2u),

  HEARTBEAT_RESPONSE(3u),

  COMMAND(5u),

  CERTIFICATE(7u),

  CERTIFICATE_RESPONSE(8u),
  ;

  companion object {
    fun fromCode(code: UInt): LiveMsgPacketType? = values().firstOrNull { it.code == code }
  }
}

package me.hbj.bikkuri.bili.packet

import io.ktor.utils.io.core.*

@OptIn(ExperimentalUnsignedTypes::class)
data class LiveMsgPacketHead(
  val size: UInt,
  val headSize: UShort,
  val protocol: LiveMsgPacketProtocol,
  val type: LiveMsgPacketType,
  val sequence: UInt,
) {
  val bodySize: UInt
    get() = size - headSize.toUInt()

  internal constructor(
    size: UInt,
    headSize: UShort,
    protocol: UShort,
    type: UInt,
    sequence: UInt,
  ) : this(
    size,
    headSize,
    protocol = LiveMsgPacketProtocol.fromCode(protocol)
      ?: error("Unknown protocol code: $protocol"),
    type = LiveMsgPacketType.fromCode(type) ?: error("Unknown type code: $type"),
    sequence,
  )

  companion object {
    fun decode(bytes: ByteArray): LiveMsgPacketHead {
      val size: UInt
      val headSize: UShort
      val protocol: UShort
      val type: UInt
      val sequence: UInt
      buildPacket {
        this.writeFully(bytes)
      }.apply {
        size = readUInt()
        headSize = readUShort()
        protocol = readUShort()
        type = readUInt()
        sequence = readUInt()
      }
      return LiveMsgPacketHead(size, headSize, protocol, type, sequence)
    }

    internal const val HEAD_SIZE: UShort = 0x10u
  }

  fun encode(): ByteArray {
    return buildPacket {
      this.writeUInt(this@LiveMsgPacketHead.size)
      this.writeUShort(headSize)
      this.writeUShort(protocol.code)
      this.writeUInt(type.code)
      this.writeUInt(sequence)
    }.readBytes()
  }
}

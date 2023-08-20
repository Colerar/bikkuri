@file:Suppress("NOTHING_TO_INLINE")

package me.hbj.bikkuri.bili.connect

import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.utils.io.core.*
import io.ktor.websocket.*
import io.ktor.websocket.FrameType.*
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.onClosed
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.onSuccess
import kotlinx.coroutines.flow.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.hbj.bikkuri.bili.BiliClient
import me.hbj.bikkuri.bili.data.live.CertificatePacketBody
import me.hbj.bikkuri.bili.data.live.CertificatePacketResponse
import me.hbj.bikkuri.bili.data.live.LiveDanmakuHost
import me.hbj.bikkuri.bili.data.live.commands.LiveCommand
import me.hbj.bikkuri.bili.data.live.commands.RawLiveCommand
import me.hbj.bikkuri.bili.packet.LiveMsgPacket
import me.hbj.bikkuri.bili.packet.LiveMsgPacketHead
import me.hbj.bikkuri.bili.packet.LiveMsgPacketProtocol.SPECIAL_NO_COMPRESSION
import me.hbj.bikkuri.bili.packet.LiveMsgPacketType.*
import kotlin.coroutines.CoroutineContext

private val logger = io.github.oshai.kotlinlogging.KotlinLogging.logger {}

private typealias Wss = DefaultClientWebSocketSession

internal class LiveMessageConnection(
  private val loginUserMid: Long,
  private val realRoomId: Long,
  private val token: String,
  private val host: LiveDanmakuHost,
  private val client: BiliClient,
  private val jsonParser: Json,
  private val context: CoroutineContext = Dispatchers.IO + CoroutineName("yabapi-live-msg-connect"),
  config: LiveDanmakuConnectConfig.() -> Unit = {},
) {
  private val httpClient = client.client
  private val configInstance = LiveDanmakuConnectConfig()

  init {
    configInstance.config()
  }

  private val sequence = atomic(0L)

  suspend fun start() = coroutineScope {
    launch(context) {
      requireNotNull(host.host)
      requireNotNull(host.wssPort)
      httpClient.wss(HttpMethod.Get, host = host.host, host.wssPort, "/sub") {
        val isSuccess = sendCertificatePacket()
        if (isSuccess) {
          launch {
            doHeartbeatJob()
          }
          handleIncoming()
        }
      }
    }
  }

  private suspend inline fun Wss.sendLiveDanmakuPacket(
    packet: LiveMsgPacket,
  ): Boolean {
    var isSuccess = false
    outgoing.trySend(Frame.byType(true, BINARY, packet.encode(), rsv1 = false, rsv2 = false, rsv3 = false)).also {
      logger.debug { "Try to send ${packet.header.type} packet." }
    }.onFailure {
      if (it is CancellationException) throw it
      logger.debug { "Failed to send ${packet.header.type} packet: $packet" }
      logger.trace(it) { "stacktrace:" }
    }.onSuccess {
      logger.debug { "Sent ${packet.header.type} packet: $packet" }
      sequence.getAndIncrement()
      logger.trace { "Now Sequence: $sequence" }
      isSuccess = true
    }.onClosed {
      if (it is CancellationException) throw it
      logger.debug { "Outgoing Channel closed" }
      cancel("Remote closed", it)
    }
    return isSuccess
  }

  private suspend inline fun Wss.handleIncoming() {
    incoming.consumeAsFlow().collect { frame ->
      when (frame) {
        is Frame.Binary -> {
          try {
            LiveMsgPacket.decode(frame.data).also { packet ->
              logger.debug { "Decoded Packet Head: ${packet.header}" }
              handleBinaryPacket(packet)
            }
          } catch (e: NotImplementedError) {
            logger.warn(e) { "Not Implemented Compression" }
          }
        }

        is Frame.Text -> logger.debug { "Received Text: ${frame.data.contentToString()}" }
        is Frame.Close -> cancel("Remote closed.")
        else -> {
          // DO NOTHING
        }
      }
    }
  }

  @OptIn(ExperimentalUnsignedTypes::class)
  private suspend inline fun Wss.handleBinaryPacket(
    packet: LiveMsgPacket,
  ) = when (packet.header.type) {
    HEARTBEAT_RESPONSE -> {
      val popular = buildPacket { writeFully(packet.body) }.readUInt()
      logger.debug { "Decoded popular value: $popular" }
      configInstance.onHeartbeatResponse(
        this,
        channelFlow {
          this.send(popular)
        },
      )
    }

    CERTIFICATE_RESPONSE -> {
      val data: CertificatePacketResponse =
        jsonParser.decodeFromString(packet.body.decodeToString())
      logger.debug { "Decoded Certificate Response: $data" }
      configInstance.onCertificateResponse(
        this,
        channelFlow {
          this.send(data)
        },
      )
    }

    COMMAND -> {
      val jsons = mutableListOf<String>()
      val body = ByteReadPacket(packet.body)
      // starts with '[' or '{'
      if (packet.body.getOrNull(0) == 123.toByte() ||
        packet.body.getOrNull(0) == 91.toByte()
      ) {
        jsons.add(packet.body.decodeToString())
      } else {
        while (!body.endOfInput && isActive) {
          require(body.remaining >= 16) { "Header is not long enough, expected: 16, actual: ${body.remaining}" }
          val headerBytes = body.readBytes(16)
          val head = LiveMsgPacketHead.decode(headerBytes)
          require(body.remaining >= head.bodySize.toLong()) {
            "Body is not long enough, expected: ${head.bodySize}, actual: ${body.remaining}"
          }
          val decodedBody = LiveMsgPacket.decode(head, body.readBytes(head.bodySize.toInt()))
          jsons.add(decodedBody.decodeToString())
        }
      }

      val flow = jsons.asFlow()
      flow.map { parsed -> // String -> RawLiveCommand
        logger.trace { "Decoded raw json string: $parsed" }
        RawLiveCommand(jsonParser.decodeFromString(parsed))
      }.collect { raw -> // Send Raw to downstream
        logger.trace { "Decoded RawLiveCommand $raw" }
        configInstance.onRawCommandResponse(this, channelFlow { send(raw) })
        val data = try {
          raw.data
        } catch (e: SerializationException) {
          logger.warn(e) { "Unexpected Serialization Exception, raw decoded: $raw" }
          null
        }
        logger.debug { "Decoded LiveCommand $data" }
        configInstance.onCommandResponse(
          this,
          channelFlow {
            data?.let { this.send(it) }
          },
        )
      }
    }

    else -> error("Decoded Unexpected Incoming Packet: $packet")
  }

  private suspend inline fun Wss.doHeartbeatJob() {
    while (isActive) {
      sendHeartbeatPacket()
      delay(30_000)
    }
  }

  private suspend inline fun Wss.sendCertificatePacket(): Boolean {
    val encodeToString = jsonParser.encodeToString(
      CertificatePacketBody(
        mid = loginUserMid,
        roomId = realRoomId,
        key = token,
        version = 2,
        platform = "web",
        buvid = client.getBuvid3()?.value ?: run {
          logger.warn { "No BUVID3 in cookies" }
          ""
        },
        type = 2,
      ),
    )
    logger.info { "CertPacket: $encodeToString" }
    val body = encodeToString.toByteArray()
    return sendLiveDanmakuPacket(
      LiveMsgPacket(
        protocol = SPECIAL_NO_COMPRESSION,
        type = CERTIFICATE,
        sequence = sequence,
        body = body,
      ),
    )
  }

  private suspend inline fun Wss.sendHeartbeatPacket() =
    sendLiveDanmakuPacket(
      LiveMsgPacket(
        protocol = SPECIAL_NO_COMPRESSION,
        type = HEARTBEAT,
        sequence = sequence,
        body = "[object Object]".toByteArray(),
      ),
    )
}

private typealias Config = LiveDanmakuConnectConfig

/**
 * 直播弹幕信息流的配置
 *
 * 将函数存储为值以供调用, 默认为空
 *
 * 流是通过 `channelFlow` 构造的, 上流是 `channel` 下流是 `flow`.
 * 因此, 并不具备冷流特性, 详见官方文档
 * @see channelFlow
 * @see Flow
 */
class LiveDanmakuConnectConfig {
  var onHeartbeatResponse: suspend Wss.(popular: Flow<UInt>) -> Unit = {}

  var onCertificateResponse: suspend Wss.(response: Flow<CertificatePacketResponse>) -> Unit = {}

  var onCommandResponse: suspend Wss.(command: Flow<LiveCommand>) -> Unit = {}

  var onRawCommandResponse: suspend Wss.(command: Flow<RawLiveCommand>) -> Unit = {}
}

inline fun Config.onHeartbeatResponse(noinline block: suspend Wss.(popular: Flow<UInt>) -> Unit) {
  onHeartbeatResponse = block
}

inline fun Config.onCertificateResponse(
  noinline block: suspend Wss.(response: Flow<CertificatePacketResponse>) -> Unit,
) {
  onCertificateResponse = block
}

inline fun Config.onCommandResponse(noinline block: suspend Wss.(command: Flow<LiveCommand>) -> Unit) {
  onCommandResponse = block
}

inline fun Config.onRawCommandResponse(noinline block: suspend Wss.(command: Flow<RawLiveCommand>) -> Unit) {
  onRawCommandResponse = block
}

package me.hbj.bikkuri.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.hbj.bikkuri.persist.DataFilePersist
import me.hbj.bikkuri.utils.prettyJson
import me.hbj.bikkuri.utils.resolveDataDirectory
import java.util.concurrent.ConcurrentHashMap

object ListenerPersist : DataFilePersist<ListenerPersist.Data>(
  resolveDataDirectory("listener.json"),
  Data(),
  Data.serializer(),
  prettyJson,
) {
  val listeners
    get() = data.listener

  @Serializable
  data class Data(
    @SerialName("listener") private var _listener: Map<Long, Listener> = ConcurrentHashMap<Long, Listener>(),
  ) {
    val listener get() = _listener as ConcurrentHashMap<Long, Listener>

    init {
      if (_listener !is ConcurrentHashMap<*, *>) {
        _listener = ConcurrentHashMap(_listener)
      }
    }
  }
}

@Serializable
data class Listener(
  var enable: Boolean = false,
  var userBind: Long? = null, // binds to bilibili mid
  var joinTimeLimit: Int = 0, // 0 for non limit
  var mode: ValidateMode = ValidateMode.SEND,
  var trigger: TimerTrigger = TimerTrigger.ON_MSG,
  var targetGroup: Long? = null,
  var kickDuration: Long = 0L,
  var recallDuration: Long = 0L,
  var queueSize: Int = 1,
)

@Serializable
enum class ValidateMode {
  RECV, SEND
}

@Serializable
enum class TimerTrigger {
  ON_JOIN, ON_MSG;

  fun toFriendly(): String = when (this) {
    ON_JOIN -> "进群时重置"
    ON_MSG -> "发消息和进群时重置"
  }
}

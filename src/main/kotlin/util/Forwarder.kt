package me.hbj.bikkuri.util

import kotlinx.datetime.Instant
import mu.KotlinLogging
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.message.data.MessageChain
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private val logger = KotlinLogging.logger {}

object Forwarder {
  private val lastForwardee = ConcurrentHashMap<Long, Long?>()
  private val lastSendTime = ConcurrentHashMap<Long, Instant>()
  suspend fun forward(to: Group, sender: NormalMember, message: MessageChain) {
    try {
      val now by lazy(LazyThreadSafetyMode.NONE) { now() }
      val last by lazy(LazyThreadSafetyMode.NONE) { lastSendTime[to.id] ?: now }
      if (lastForwardee[to.id] != sender.id || (now - last) > 3.toDuration(DurationUnit.MINUTES)) {
        lastSendTime[to.id] = now
        to.sendMessage("转发自 ${sender.nameCardOrNick} 的消息: ")
      }
      to.sendMessage(message)
    } catch (e: Exception) {
      logger.error(e) { "Failed to send message, $this" }
      to.sendMessage("转发失败…… 该消息类型可能暂未支持")
    } finally {
      lastForwardee[to.id] = sender.id
    }
  }
}

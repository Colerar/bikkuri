package me.hbj.bikkuri.events

import kotlinx.coroutines.delay
import me.hbj.bikkuri.data.General
import me.hbj.bikkuri.data.RandomReplyMode.BETWEEN
import me.hbj.bikkuri.data.RandomReplyMode.FIXED
import me.hbj.bikkuri.data.RandomReplyMode.MATH_LOG
import me.hbj.bikkuri.data.RandomReplyMode.NONE
import me.hbj.bikkuri.data.RandomReplyMode.SCALE
import net.mamoe.mirai.event.events.MessagePreSendEvent
import net.mamoe.mirai.message.data.content
import kotlin.math.log
import kotlin.math.max
import kotlin.math.pow
import kotlin.random.Random

private val logger = mu.KotlinLogging.logger { }

fun Events.onMessagePreSend() {
  subscribeAlways<MessagePreSendEvent> {
    val rand = General.randomReply

    val len by lazy { message.content.length }

    val delay: Long = when (rand.mode) {
      NONE -> 0L
      FIXED -> rand.fixedValue
      BETWEEN -> {
        val (x, y) = rand.betweenRange
        (x..y).random()
      }
      SCALE -> {
        val (x, y) = rand.scaleCoefficient
        (len * Random.nextDouble(x, y)).toLong()
      }
      MATH_LOG -> with(rand.log) {
        // e.g. 20 * log_{2}^{10} + 300
        val computed = (coefficient * log(len.toDouble().pow(pow), base) + constant).toLong()
        computed + Random.nextLong(-jitter, jitter)
      }
    }.also {
      logger.debug { "Send Message Delay: $it" }
    }
    delay(max(0L, delay))
  }
}

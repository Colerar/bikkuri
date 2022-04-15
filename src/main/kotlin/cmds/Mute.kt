package me.hbj.bikkuri.cmds

import me.hbj.bikkuri.Bikkuri
import me.hbj.bikkuri.util.parseMessageMember
import me.hbj.bikkuri.util.parseTime
import me.hbj.bikkuri.util.requireOperator
import me.hbj.bikkuri.util.toFriendly
import net.mamoe.mirai.console.command.MemberCommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.message.data.MessageChain
import kotlin.math.min
import kotlin.time.DurationUnit
import kotlin.time.toDuration

object Mute : SimpleCommand(Bikkuri, "mute"), RegisteredCmd {
  // 30*24*60*60, a month
  private const val MAX_MUTE_TIME: Long = 2592000

  private fun parseMuteTime(expr: String): Int? {
    val dur = parseTime(expr) ?: return null
    return min(dur.inWholeSeconds, MAX_MUTE_TIME).toInt()
  }

  @Handler
  suspend fun MemberCommandSender.handle(message: MessageChain, duration: String) {
    requireOperator(this)
    parseMessageMember(message,
      onMember = {
        val muteTime = parseMuteTime(duration) ?: run {
          sendMessage("输入的表达式错误。")
          return@parseMessageMember
        }
        it.mute(muteTime)
        sendMessage("已成功禁言 ${it.toFriendly()} ${muteTime.toDuration(DurationUnit.SECONDS).toFriendly()}")
      },
      onId = {
        sendMessage("找不到输入的群员。")
      }
    )
  }
}

object Unmute : SimpleCommand(Bikkuri, "unmute"), RegisteredCmd {
  @Handler
  suspend fun MemberCommandSender.handle(message: MessageChain) {
    requireOperator(this)
    parseMessageMember(message,
      onMember = {
        if (it.isMuted) {
          it.unmute()
          sendMessage("已成功解禁 ${it.toFriendly()}")
        } else {
          sendMessage("${it.toFriendly()} 没有被禁言哦~")
        }
      },
      onId = { sendMessage("找不到输入的群员。") }
    )
  }
}

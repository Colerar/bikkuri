package me.hbj.bikkuri.validator

import me.hbj.bikkuri.client
import me.hbj.bikkuri.data.KeygenData
import me.hbj.bikkuri.data.fitKeygen
import moe.sdl.yabapi.api.fetchSessionMessage
import moe.sdl.yabapi.api.getBasicInfo
import moe.sdl.yabapi.data.message.contents.Text
import net.mamoe.mirai.console.command.CommandSender.Companion.toCommandSender
import net.mamoe.mirai.console.command.MemberCommandSender
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.content
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private val logger = mu.KotlinLogging.logger {}

class RecvMessageValidator(
  private val keygen: KeygenData,
  private val uid: Int,
) : MessageValidatorWithLoop {
  override val loopInterval: Duration
    get() = 10.toDuration(DurationUnit.SECONDS)

  override suspend fun beforeValidate(sender: MemberCommandSender) {
    val basicInfo = client.getBasicInfo().data
    sender.sendMessage(
      """
      [${keygen.keygen}]
      麻烦您按顺序完成以下两个步骤：
      ① 复制整条消息，b站私信验证机器人：${basicInfo.username} （注：uid${basicInfo.mid}）
      ② 私信后返回本群发送任何消息开始验证。（本条 ${keygen.expire} 秒内有效）
      """.trimIndent()
    )
    sender.sendMessage("网页版直达链接: https://message.bilibili.com/#/whisper/mid${basicInfo.mid}")
  }

  override suspend fun validate(event: GroupMessageEvent): ValidatorOperation {
    if (event.message.content == "quit") return ValidatorOperation.FAILED
    return validateLoop(event.toCommandSender()).also {
      if (it != ValidatorOperation.PASSED)
        event.group.sendMessage("验证失败，稍后重试看看... 如有问题请 @ 管理，发送 quit 可退出验证。")
    }
  }

  override suspend fun validateLoop(member: MemberCommandSender): ValidatorOperation {
    val pass = client.fetchSessionMessage(uid, size = 10).data?.messages.orEmpty().asSequence()
      .map { it.content }
      .filterIsInstance<Text>()
      .any { it.content.fitKeygen(keygen) }
    if (pass) return ValidatorOperation.PASSED
    return ValidatorOperation.CONTINUED
  }
}

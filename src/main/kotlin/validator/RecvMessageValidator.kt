package me.hbj.bikkuri.validator

import me.hbj.bikkuri.client
import me.hbj.bikkuri.data.KeygenData
import me.hbj.bikkuri.data.fitKeygen
import me.hbj.bikkuri.utils.sendMessage
import moe.sdl.yabapi.api.fetchSessionMessage
import moe.sdl.yabapi.api.getBasicInfo
import moe.sdl.yabapi.data.message.contents.Text
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.QuoteReply
import net.mamoe.mirai.message.data.content
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class RecvMessageValidator(
  private val keygen: KeygenData,
  private val uid: Long,
) : MessageValidatorWithLoop {
  override val loopInterval: Duration
    get() = 10.toDuration(DurationUnit.SECONDS)

  override suspend fun beforeValidate(sender: NormalMember) {
    val basicInfo = client.getBasicInfo().data
    sender.group.sendMessage {
      +At(sender)
      +" "
      +"""
      请您按以下操作完成最后一步：
      1. 前往 B 站私信验证机器人：${basicInfo.username} （UID:${basicInfo.mid}），发送验证码 [${keygen.keygen}]。
      2. 若无法发送私信，请先关注机器人。
      3. 返回 QQ，发送任意消息完成验证。
      """.trimIndent()
    }
    sender.group.sendMessage("网页版直达链接: https://message.bilibili.com/#/whisper/mid${basicInfo.mid}")
  }

  override suspend fun validate(event: GroupMessageEvent): ValidatorOperation {
    if (event.message.content == "quit") return ValidatorOperation.FAILED
    return validateLoop(event.sender as NormalMember).also {
      if (it != ValidatorOperation.PASSED) {
        event.group.sendMessage {
          +QuoteReply(event.message)
          +" 验证失败，稍后重试看看... 如有问题请 @ 管理，发送 quit 可退出验证。"
        }
      }
    }
  }

  override suspend fun validateLoop(member: NormalMember): ValidatorOperation {
    val pass = client.fetchSessionMessage(uid, size = 10).data?.messages.orEmpty().asSequence()
      .map { it.content }
      .filterIsInstance<Text>()
      .any { it.content.fitKeygen(keygen) }
    if (pass) return ValidatorOperation.PASSED
    return ValidatorOperation.CONTINUED
  }
}

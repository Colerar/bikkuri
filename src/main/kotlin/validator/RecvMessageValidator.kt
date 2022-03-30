package me.hbj.bikkuri.validator

import me.hbj.bikkuri.client
import me.hbj.bikkuri.data.KeygenData
import me.hbj.bikkuri.data.fitKeygen
import moe.sdl.yabapi.api.fetchSessionMessage
import moe.sdl.yabapi.api.getBasicInfo
import moe.sdl.yabapi.data.message.contents.Text
import net.mamoe.mirai.console.command.MemberCommandSender
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.content

private val logger = mu.KotlinLogging.logger {}

class RecvMessageValidator(
  private val keygen: KeygenData,
  private val uid: Int,
) : MessageValidator {
  override suspend fun beforeValidate(sender: MemberCommandSender) {
    val basicInfo = client.getBasicInfo().data
    sender.sendMessage(
      """
      请私信 B 站用户 ${basicInfo.username} (UID ${basicInfo.mid}) 验证码完成验证, ${keygen.expire} 秒内有效：[${keygen.keygen}]
      网页版直达链接: https://message.bilibili.com/#/whisper/mid${basicInfo.mid}
      完成后发送任意信息验证，如遇问题请发送 "quit" 退出后联系管理
      """.trimIndent()
    )
  }

  override suspend fun validate(event: GroupMessageEvent): ValidatorOperation {
    if (event.message.content == "quit") return ValidatorOperation.FAILED
    val pass = client.fetchSessionMessage(uid, size = 10).data?.messages.orEmpty().asSequence()
      .map { it.content }
      .filterIsInstance<Text>()
      .any { it.content.fitKeygen(keygen) }
    if (pass) return ValidatorOperation.PASSED else {
      event.group.sendMessage("验证失败，稍后重试看看...")
    }
    return ValidatorOperation.CONTINUED
  }
}

package me.hbj.bikkuri.validator

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import me.hbj.bikkuri.client
import me.hbj.bikkuri.data.GroupListener
import me.hbj.bikkuri.data.KeygenData
import me.hbj.bikkuri.data.fitKeygen
import moe.sdl.yabapi.api.getBasicInfo
import moe.sdl.yabapi.api.getUserCard
import moe.sdl.yabapi.api.sendMessageTo
import moe.sdl.yabapi.data.GeneralCode
import moe.sdl.yabapi.data.message.MessageContent
import net.mamoe.mirai.console.command.MemberCommandSender
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.message.data.content

private val logger = mu.KotlinLogging.logger {}

class SendMessageValidator(
  private val keygen: KeygenData,
  private val uid: Int,
  private val data: GroupListener,
) : MessageValidator {
  private val scope = CoroutineScope(Dispatchers.IO)

  override suspend fun beforeValidate(sender: MemberCommandSender) {
    val biliSenderData = client.getBasicInfo()
    val bindName = scope.async {
      client.getUserCard(data.userBind!!.toInt(), false).data?.card?.name ?: run {
        sender.group.sendMessage("获取绑定 UP 主信息失败~")
        data.userBind.toString()
      }
    }

    listOf(
      scope.launch {
        sender.group.sendMessage("请回复B站用户 [${biliSenderData.data.username}] 给你私信的验证码, ${keygen.expire} 秒后超时, 请尽快哦~")
      },
      scope.launch {
        logger.info { "Try to send message..." }
        client.sendMessageTo(
          uid,
          MessageContent.Text(
            "${bindName.await()}舰长群的入群验证码: [${keygen.keygen}], " +
              "${keygen.expire} 秒内有效, 非本人操作请忽略 (可直接复制整段文字)"
          )
        ).also {
          logger.info { "Send message response: $it" }
          if (it.code != GeneralCode.SUCCESS) {
            logger.warn { "Failed send message, ${it.code} - ${it.message}, $uid" }
            sender.group.sendMessage("私信发送失败，请联系管理员, 错误原因: ${it.code}-${it.message}")
            throw CancellationException("Failed to send bilibili message to $uid")
          }
        }
      },
    ).joinAll()
  }

  override suspend fun validate(event: GroupMessageEvent): ValidatorOperation {
    when (event.message.content) {
      "quit" -> {
        event.group.sendMessage("退出审核~")
        return ValidatorOperation.FAILED
      }
      else -> {
        val keygenFit = event.message.content.fitKeygen(keygen)
        val keygenNotExpired = keygen.expiresAt > Clock.System.now()

        return if (keygenFit && keygenNotExpired) {
          ValidatorOperation.PASSED
        } else {
          val msg = when {
            !keygenFit -> buildMessageChain { add("呜~ 验证码错误, 可再次输入验证码重试, quit 退出.") }
            @Suppress("KotlinConstantConditions") !keygenNotExpired ->
              buildMessageChain { add("呜~ 验证码过期, 可再次输入验证码重试, quit 退出.") }
            else -> buildMessageChain { add("审核失败~可再次输入验证码重试, quit 退出.") }
          }
          event.group.sendMessage(msg)
          ValidatorOperation.FAILED
        }
      }
    }
  }
}

package me.hbj.bikkuri.validator

import kotlinx.coroutines.*
import me.hbj.bikkuri.bili.api.getBasicInfo
import me.hbj.bikkuri.bili.api.getUserCard
import me.hbj.bikkuri.bili.api.sendMessageTo
import me.hbj.bikkuri.bili.data.GeneralCode
import me.hbj.bikkuri.bili.data.message.MessageContent
import me.hbj.bikkuri.client
import me.hbj.bikkuri.data.KeygenData
import me.hbj.bikkuri.data.Listener
import me.hbj.bikkuri.data.fitKeygen
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.content

private val logger = io.github.oshai.kotlinlogging.KotlinLogging.logger {}

class SendMessageValidator(
  private val keygen: KeygenData,
  private val uid: Long,
  private val data: Listener,
) : MessageValidator {
  private val scope = CoroutineScope(Dispatchers.IO)

  override suspend fun beforeValidate(sender: NormalMember) {
    val biliSenderData = client.getBasicInfo()
    val bindName = scope.async {
      client.getUserCard(data.userBind!!, false).data?.card?.name ?: run {
        sender.group.sendMessage("获取绑定 UP 主信息失败~")
        data.userBind.toString()
      }
    }

    listOf(
      scope.launch {
        sender.group.sendMessage("请回复B站用户 [${biliSenderData.data.username}] 给你私信的验证码, 请尽快哦~")
      },
      scope.launch {
        logger.info { "Try to send message..." }
        client.sendMessageTo(
          uid,
          MessageContent.Text(
            "${bindName.await()}舰长群的入群验证码: [${keygen.keygen}], 非本人操作请忽略 (可直接复制整段文字)",
          ),
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

        return if (keygenFit) {
          ValidatorOperation.PASSED
        } else {
          event.group.sendMessage("呜~ 验证码错误, 可再次输入验证码重试, quit 退出.")
          ValidatorOperation.FAILED
        }
      }
    }
  }
}

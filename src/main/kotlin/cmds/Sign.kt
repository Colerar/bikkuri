package me.hbj.bikkuri.cmds

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.datetime.Clock
import me.hbj.bikkuri.Bikkuri
import me.hbj.bikkuri.Bikkuri.logger
import me.hbj.bikkuri.client
import me.hbj.bikkuri.data.AutoApprove
import me.hbj.bikkuri.data.AutoApproveData
import me.hbj.bikkuri.data.Keygen
import me.hbj.bikkuri.data.KeygenData
import me.hbj.bikkuri.data.ListenerData
import me.hbj.bikkuri.util.addImageOrText
import me.hbj.bikkuri.util.loadImageResource
import moe.sdl.yabapi.api.getBasicInfo
import moe.sdl.yabapi.api.getUserCard
import moe.sdl.yabapi.api.getUserSpace
import moe.sdl.yabapi.api.sendMessageTo
import moe.sdl.yabapi.data.message.MessageContent
import net.mamoe.mirai.console.command.MemberCommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.nextEvent
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.debug
import kotlin.time.DurationUnit.SECONDS
import kotlin.time.toDuration

private const val REQUIRED_LEVEL = 21

object Sign : SimpleCommand(Bikkuri, "sign", "s", "验证") {
  @Handler
  suspend fun MemberCommandSender.handle() {
    val data = ListenerData.map[group.id]
    if (data?.enable == false) return
    if (data?.userBind == null || data.targetGroup == null) {
      group.sendMessage("配置不完整，请联系管理员")
      return
    }

    val biliSenderData = coroutineScope { async { client.getBasicInfo() } }

    val bindName = coroutineScope {
      withTimeoutOrNull(5_000) {
        async {
          client.getUserCard(data.userBind!!.toInt(), false).data?.card?.name ?: run {
            group.sendMessage("获取绑定 UP 主信息失败~")
            data.userBind.toString()
          }
        }
      }
    }

    group.sendMessage("请发送 B 站 UID~")

    var uid: Int? = null

    suspend fun nextMsgEvent() = GlobalEventChannel.nextEvent<GroupMessageEvent> {
      it.group.id == user.group.id && it.sender.id == user.id
    }

    val sec = 300L

    var loop1 = true
    while (loop1) {
      nextMsgEvent().apply {
        if (message.content == "quit") {
          loop1 = false
          return@apply
        }

        uid = Regex("""^\s*(UID:)?([0-9]+)\s*$""").find(message.content)?.groupValues?.get(2)?.toIntOrNull() ?: run {
          group.sendMessage("输入错误, 需要纯数字 UID, 请重新输入")
          return@apply
        }
        group.sendMessage("稍等正在查询中……")

        val medal = client.getUserSpace(uid!!).data?.fansMedal?.medal

        val medalNotNull = medal != null
        val medalUserFit = medal?.targetId == data.userBind?.toInt()
        val medalLevel = medal?.level?.let { it >= REQUIRED_LEVEL } ?: false

        when {
          !medalNotNull -> buildMessageChain {
            add("呜~ 未获取到粉丝牌信息, 请根据下图指引佩戴粉丝牌~(再次发送 UID 重试, quit 退出)")
            addImageOrText(loadImageResource("./images/guide-phone.jpg", "jpg"), group)
            addImageOrText(loadImageResource("./images/guide-web.jpg", "jpg"), group)
          }
          !medalUserFit -> buildMessageChain {
            add("呜~ 请佩戴 [${bindName?.await()}] 的粉丝牌哦! 可再次输入 UID 重试, quit 退出.")
          }
          !medalLevel -> buildMessageChain {
            add("粉丝牌等级不足~ 需要至少 $REQUIRED_LEVEL 级哦~")
          }
          else -> buildMessageChain {
            add("请回复B站用户 [${biliSenderData.await().data.username}] 给你发送的验证码, $sec 秒后超时, 请尽快哦~")
          }.also {
            loop1 = false
          }
        }.also {
          group.sendMessage(it)
        }
      }
    }

    @Suppress("KotlinConstantConditions")
    if (uid == null) {
      logger.debug { "Uid is null, return..." }
      return
    }

    logger.debug { "Generating Keygen..." }
    val keygen = KeygenData(uid.toString(), 6, sec.toDuration(SECONDS))
    Keygen.map[user.id] = keygen

    logger.debug { "Try to send message..." }
    client.sendMessageTo(
      uid!!, MessageContent.Text("""<${bindName?.await()}> 舰长群的入群审核码: [${keygen.keygen}], $sec 秒内有效, 如非本人操作请忽略 (可直接复制整段文字)""")
    ).also {
      logger.debug { "Send message response: $it" }
    }

    withTimeoutOrNull(sec * 1000) {
      logger.debug { "Waiting for response" }
      var loop = true
      while (loop) {
        nextMsgEvent().apply {
          when (message.content) {
            "quit" -> {
              group.sendMessage("退出审核~")
              loop = false
            }
            else -> {
              val str = Regex("""(\[?[0-9A-Za-z]+]?)""").find(message.content)?.value
              val keygenFit = str?.removeSurrounding("[", "]") == keygen.keygen
              val keygenNotExpired = keygen.expiresAt > Clock.System.now()

              if (keygenFit && keygenNotExpired) {
                group.sendMessage(
                  """
                    成功通过审核~~~ 舰长群号已经私聊给你，申请后会自动同意。
                    【❗重要：入群后先阅读群规，否则后果自负！】如有其他审核问题请联系管理员。
                  """.trimIndent()
                )
                user.sendMessage("舰长群号: ${data.targetGroup}, 请勿外传!")
                AutoApprove.map.getOrPut(data.targetGroup!!) { AutoApproveData() }
                AutoApprove.map[data.targetGroup!!]?.set?.add(user.id) ?: run {
                  group.sendMessage("将你添加到自动批准列表时出现错误, 请联系管理员")
                }
                loop = false
              } else group.sendMessage(
                when {
                  !keygenFit -> buildMessageChain {
                    add("呜~ 验证码错误, 可再次输入验证码重试, quit 退出.")
                  }
                  @Suppress("KotlinConstantConditions") !keygenNotExpired -> buildMessageChain {
                    add("呜~ 验证码过期, 可再次输入验证码重试, quit 退出.")
                  }
                  else -> buildMessageChain {
                    add("审核失败~可再次输入验证码重试, quit 退出.")
                  }
                }
              )
            }
          }
        }
      }
    }
  }
}

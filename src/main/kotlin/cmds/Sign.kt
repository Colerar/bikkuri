package me.hbj.bikkuri.cmds

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.datetime.Clock
import me.hbj.bikkuri.Bikkuri
import me.hbj.bikkuri.client
import me.hbj.bikkuri.data.*
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
import net.mamoe.mirai.message.data.content
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
      async {
        client.getUserCard(data.userBind!!.toInt(), false).data?.card?.name
            ?: run {
              group.sendMessage("获取绑定 UP 主信息失败~")
              data.userBind.toString()
            }
      }
    }

    group.sendMessage("请发送 B 站 UID~")

    var uid: Int? = null

    suspend fun nextMsgEvent() = GlobalEventChannel.nextEvent<GroupMessageEvent> {
      it.group.id == user.group.id && it.sender.id == user.id
    }

    var loop1 = true
    while (loop1) {
      nextMsgEvent().apply {
        uid = Regex("""^\s*(UID:)?([0-9]+)\s*$""")
            .find(message.content)?.groupValues?.get(2)
            ?.toIntOrNull()
            ?: run {
              group.sendMessage("输入错误, 需要纯数字 UID, 请重新输入")
              return@apply
            }
        loop1 = false
      }
    }

//        if (uid == null) return

    val sec = 300L
    val keygen = KeygenData(uid.toString(), 6, sec.toDuration(SECONDS))
    Keygen.map[user.id] = keygen
    coroutineScope {
      launch {
        client.sendMessageTo(
            uid!!,
            MessageContent.Text("""【${bindName.await()}】 舰长群的入群审核码: [${keygen.keygen}], $sec 秒内有效, 如非本人操作请忽略""")
        )
      }
    }

    group.sendMessage(
        """
          为了完成审核有两个要件~
          1. 请挂上 [${bindName.await()}] 的粉丝牌子
          2. 回复B站用户 [${biliSenderData.await().data.username}] 给你发送的验证码, $sec 秒后超时, 请尽快哦~
      """.trimIndent()
    )

    var loop = true
    withTimeoutOrNull(sec * 1000) {
      while (loop) {
        nextMsgEvent().apply {
          when (message.content) {
            "quit" -> {
              group.sendMessage("退出审核~")
              loop = false
            }
            else -> {
              val passed = client.getUserSpace(uid!!).data?.run {
                val medal = fansMedal?.medal
                medal?.targetId == data.userBind?.toInt()
                    && medal?.isLighted == true
                    && medal.level?.let { it >= REQUIRED_LEVEL } ?: false
                    && message.content.removeSurrounding("[", "]") == keygen.keygen
                    && keygen.expiresAt > Clock.System.now()
              }
              if (passed == true) {
                group.sendMessage("成功通过审核~~~ 请手动申请加群: ${data.targetGroup}, 申请后会自动同意, 如有问题请联系管理员")
                AutoApprove.map.getOrPut(data.targetGroup!!) { AutoApproveData() }
                AutoApprove.map[data.targetGroup!!]?.list?.add(user.id) ?: run {
                  group.sendMessage("添加到自动批准列表时出现错误, 请联系管理员")
                }
                loop = false
              } else {
                group.sendMessage("审核失败~可再次输入验证码重试, quit 退出.")
              }
            }
          }
        }
      }
    }
  }
}

package me.hbj.bikkuri.cmds

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import me.hbj.bikkuri.Bikkuri
import me.hbj.bikkuri.client
import me.hbj.bikkuri.data.General
import me.hbj.bikkuri.data.GlobalAutoApprove
import me.hbj.bikkuri.data.Keygen
import me.hbj.bikkuri.data.KeygenData
import me.hbj.bikkuri.data.ListenerData
import me.hbj.bikkuri.data.MemberToApprove
import me.hbj.bikkuri.data.ValidateMode
import me.hbj.bikkuri.db.GuardList
import me.hbj.bikkuri.db.addBlock
import me.hbj.bikkuri.db.isBiliBlocked
import me.hbj.bikkuri.db.isBlocked
import me.hbj.bikkuri.exception.command.CommandCancellation
import me.hbj.bikkuri.util.ModuleScope
import me.hbj.bikkuri.util.addImageOrText
import me.hbj.bikkuri.util.loadImageResource
import me.hbj.bikkuri.util.toFriendly
import me.hbj.bikkuri.util.uidRegex
import me.hbj.bikkuri.validator.MessageValidatorWithLoop
import me.hbj.bikkuri.validator.RecvMessageValidator
import me.hbj.bikkuri.validator.SendMessageValidator
import me.hbj.bikkuri.validator.ValidatorOperation
import moe.sdl.yabapi.api.getUserSpace
import moe.sdl.yabapi.api.modifyRelation
import moe.sdl.yabapi.enums.relation.RelationAction
import moe.sdl.yabapi.enums.relation.SubscribeSource
import mu.KotlinLogging
import net.mamoe.mirai.console.command.MemberCommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.nextEvent
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.message.data.content
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private val logger = KotlinLogging.logger {}

object Sign : SimpleCommand(Bikkuri, "sign", "s", "验证"), RegisteredCmd {
  private val signScope = ModuleScope("SignCommand")

  @Handler
  suspend fun MemberCommandSender.handle() {
    val data = ListenerData.map[group.id]
    if (data?.enable == false) return
    if (data?.userBind == null || data.targetGroup == null) {
      group.sendMessage("配置不完整，请联系管理员")
      return
    }
    var uid: Int? = null

    val expireDuration = General.keygen.timeout.toDuration(DurationUnit.MILLISECONDS)

    val keygen by lazy {
      logger.info { "Generating Keygen..." }
      KeygenData(uid.toString(), General.keygen.length, expireDuration).also {
        Keygen.map[user.id] = it
        logger.info { "Generated Keygen $it" }
      }
    }

    suspend fun nextMsgEvent() = GlobalEventChannel.nextEvent<GroupMessageEvent> {
      it.group.id == user.group.id && it.sender.id == user.id
    }

    group.sendMessage("请发送 B 站 UID~")

    var loop1 = true
    while (loop1) nextMsgEvent().apply {
      if (message.content == "quit") {
        loop1 = false
        return
      }

      uid = uidRegex.find(message.content)?.groupValues?.get(2)?.toIntOrNull() ?: run {
        group.sendMessage("输入错误, 需要纯数字 UID, 请重新输入")
        return@apply
      }

      val userSpace = coroutineScope {
        withTimeoutOrNull(30_000) {
          async {
            client.getUserSpace(uid!!)
          }
        }
      }

      coroutineScope {
        launch {
          group.sendMessage("稍等正在查询中……")
        }
        launch {
          if (uid != null && group.isBiliBlocked(uid?.toLong() ?: return@launch)) {
            launch {
              (this@handle.user as NormalMember).kick("你已被拉黑。")
            }
            val buser = run {
              val name = userSpace?.await()?.data?.name
              name?.let { "$name ($uid)" } ?: uid.toString()
            }
            val msg = buildMessageChain {
              add("因 B 站用户 $buser 已被拉黑，故将其踢出本群。")
              if (group.isBlocked(this@handle.user.id)) {
                group.addBlock(this@handle.user.id)
                add("同时将其本次申请的 QQ ${user.toFriendly()} 拉黑。")
              }
            }
            sendMessage(msg)
            throw CommandCancellation(Sign)
          }
        }
      }

      val medal = userSpace?.await()?.data?.fansMedal?.medal

      val inList = GuardList.validate(data.userBind!!, uid!!.toLong())

      val medalNotNull = medal != null
      val medalUserFit = medal?.targetId == data.userBind?.toInt()
      val medalLevel = medal?.level?.let { it >= General.joinRequiredLevel } ?: false

      when {
        inList || (medalNotNull && medalUserFit && medalLevel) -> loop1 = false
        else -> {
          val msg = when {
            !medalNotNull -> buildMessageChain {
              add("呜~ 未获取到粉丝牌信息, 请根据下图指引佩戴粉丝牌~(再次发送 UID 重试, quit 退出)")
              addImageOrText(loadImageResource("./images/guide-phone.jpg"), group)
              addImageOrText(loadImageResource("./images/guide-web.jpg"), group)
            }

            !medalUserFit -> buildMessageChain {
              add("呜~ 请佩戴正确的粉丝牌哦! 可再次输入 UID 重试, quit 退出")
            }

            @Suppress("KotlinConstantConditions")
            !medalLevel -> buildMessageChain {
              add("粉丝牌等级不足~ 需要至少 ${General.joinRequiredLevel} 级哦~")
            }

            else -> buildMessageChain {
              add("遇到未知错误，可再次输入 UID 重试, quit 退出")
            }
          }
          group.sendMessage(msg)
        }
      }
    }

    @Suppress("KotlinConstantConditions")
    if (uid == null) {
      logger.info { "Uid is null, return..." }
      return
    }

    val validator = when (data.mode) {
      ValidateMode.SEND -> SendMessageValidator(keygen, uid!!, data)
      ValidateMode.RECV -> RecvMessageValidator(keygen, uid!!)
    }

    coroutineScope {
      launch { validator.beforeValidate(this@handle) }
      launch { client.modifyRelation(uid!!, RelationAction.SUB, SubscribeSource.values().random()) }
    }

    var passed by atomic(false)
    suspend fun whenPassed() {
      if (passed) return
      passed = true
      val map = GlobalAutoApprove[bot.id][data.targetGroup!!].map
      map[this.user.id] = MemberToApprove(
        uid!!.toLong(),
        this.group.id,
      )
      signScope.launch {
        group.sendMessage(
          """
          成功通过审核~~~ 舰长群号 ${data.targetGroup}，申请后会自动同意。
          【❗重要：入群后先阅读群规，否则后果自负！】如有其他审核问题请联系管理员。
          """.trimIndent()
        )
      }
    }

    coroutineScope {
      val waitReply: Job?
      var loopJob: Job? = null

      waitReply = launch {
        withTimeoutOrNull(expireDuration * 1000) {
          logger.debug { "Waiting for response" }
          var loop = true

          while (loop) nextMsgEvent().apply {
            loop = when (validator.validate(this)) {
              ValidatorOperation.CONTINUED -> true
              ValidatorOperation.PASSED -> {
                whenPassed()
                false
              }

              else -> false
            }
            if (!loop) loopJob?.cancel()
          }
        }
      }

      loopJob = if (validator is MessageValidatorWithLoop) {
        launch {
          while (isActive) {
            when (validator.validateLoop(this@handle)) {
              ValidatorOperation.PASSED -> {
                whenPassed()
                waitReply.cancel()
                return@launch
              }

              ValidatorOperation.FAILED -> throw CommandCancellation(Sign)
              ValidatorOperation.CONTINUED -> {
                // continue
              }
            }
            delay(validator.loopInterval)
          }
        }
      } else null

      listOfNotNull(waitReply, loopJob).joinAll()
    }

    coroutineScope {
      launch { client.modifyRelation(uid!!, RelationAction.UNSUB, SubscribeSource.values().random()) }
    }
  }
}

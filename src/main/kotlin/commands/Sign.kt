package me.hbj.bikkuri.commands

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import me.hbj.bikkuri.api.getUserSpaceWbi
import me.hbj.bikkuri.client
import me.hbj.bikkuri.command.Command
import me.hbj.bikkuri.command.MiraiCommandSender
import me.hbj.bikkuri.data.KeygenData
import me.hbj.bikkuri.data.Listener
import me.hbj.bikkuri.data.ListenerPersist
import me.hbj.bikkuri.data.ValidateMode
import me.hbj.bikkuri.db.GuardList
import me.hbj.bikkuri.db.addBlock
import me.hbj.bikkuri.db.isBiliBlocked
import me.hbj.bikkuri.db.isBlocked
import me.hbj.bikkuri.events.AutoApprove
import me.hbj.bikkuri.events.globalAutoApprove
import me.hbj.bikkuri.utils.addImageOrText
import me.hbj.bikkuri.utils.loadImageResource
import me.hbj.bikkuri.utils.sendMessage
import me.hbj.bikkuri.utils.toFriendly
import me.hbj.bikkuri.validator.MessageValidatorWithLoop
import me.hbj.bikkuri.validator.RecvMessageValidator
import me.hbj.bikkuri.validator.SendMessageValidator
import me.hbj.bikkuri.validator.ValidatorOperation
import moe.sdl.yac.core.CliktError
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.nextEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.QuoteReply
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.message.data.content
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedDeque

private val logger = KotlinLogging.logger {}

internal val uidRegex = Regex("""^\s*(UID[:：]?)?([0-9]+)\s*$""")

class Sign(private val sender: MiraiCommandSender) : Command(Sign) {
  val member = run {
    if (sender.contact !is NormalMember) throw CliktError()
    sender.contact
  }

  val data = ListenerPersist.listeners.computeIfAbsent(member.group.id) { Listener() }

  val event = sender.event
  val message = event.message

  override suspend fun run(): Unit = coroutineScope cmd@{
    if (!data.enable) return@cmd
    if (data.userBind == null || data.targetGroup == null) {
      sender.sendMessage("配置不完整，请联系管理员")
      return@cmd
    }
    var uid: Long? = null

    suspend fun nextMsgEvent() = GlobalEventChannel.nextEvent<GroupMessageEvent> {
      it.group.id == member.group.id && it.sender.id == member.id
    }

    member.group.sendMessage {
      +At(member)
      +" 请发送 B 站 UID~"
    }

    var loop1 = true
    while (loop1 && isActive) nextMsgEvent().apply {
      if (message.content == "quit") {
        loop1 = false
        return@cmd
      }

      uid = uidRegex.find(message.content)?.groupValues?.get(2)?.toLongOrNull() ?: run {
        group.sendMessage {
          +QuoteReply(message)
          +"输入错误, 需要纯数字 UID, 请重新输入"
        }
        return@apply
      }

      val userSpace =
        withTimeoutOrNull(30_000) {
          async {
            client.getUserSpaceWbi(uid!!)
          }
        }

      launch {
        group.sendMessage("稍等正在查询中……")
      }
      launch {
        if (uid != null && group.isBiliBlocked(uid ?: return@launch)) {
          launch {
            member.kick("你已被拉黑。")
          }
          val bUser = run {
            val name = userSpace?.await()?.data?.name
            name?.let { "$name ($uid)" } ?: uid.toString()
          }
          val msg = buildMessageChain {
            add("因 B 站用户 $bUser 已被拉黑，故将其踢出本群。")
            if (group.isBlocked(member.id)) {
              group.addBlock(member.id)
              add("同时将其本次申请的 QQ ${member.toFriendly()} 拉黑。")
            }
          }
          member.sendMessage(msg)
          throw CliktError("Command Cancellation")
        }
      }

      val medal = userSpace?.await()?.data?.fansMedal?.medal

      val inList = GuardList.validate(data.userBind!!, uid!!.toLong())

      val medalNotNull = medal != null
      val medalUserFit = medal?.targetId == data.userBind
      val medalLevel = medal?.level?.let { it >= 21 } ?: false

      @Suppress("KotlinConstantConditions")
      when {
        inList || (medalNotNull && medalUserFit && medalLevel) -> loop1 = false
        else -> {
          val msg = when {
            !medalNotNull -> buildMessageChain {
              +QuoteReply(message)
              +"呜~ 未获取到粉丝牌信息, 请根据下图指引佩戴粉丝牌~(再次发送 UID 重试, quit 退出)"
              addImageOrText(loadImageResource("./guide-phone.jpg"), group)
              addImageOrText(loadImageResource("./guide-web.jpg"), group)
            }

            !medalUserFit -> buildMessageChain {
              +QuoteReply(message)
              +"呜~ 请佩戴正确的粉丝牌哦! 可再次输入 UID 重试, quit 退出"
            }

            !medalLevel -> buildMessageChain {
              +QuoteReply(message)
              +"粉丝牌等级不足~ 需要至少 21 级哦~"
            }

            else -> buildMessageChain {
              +QuoteReply(message)
              +"遇到未知错误，可再次输入 UID 重试, quit 退出"
            }
          }
          group.sendMessage(msg)
        }
      }
    }

    if (uid == null) {
      logger.info { "Uid is null, return..." }
      return@cmd
    }

    val keygen = KeygenData(6)

    val validator = when (data.mode) {
      ValidateMode.SEND -> SendMessageValidator(keygen, uid!!, data)
      ValidateMode.RECV -> RecvMessageValidator(keygen, uid!!)
    }

    validator.beforeValidate(member)

    var passed by atomic(false)
    suspend fun whenPassed() {
      if (passed) return
      passed = true
      val groupMap =
        globalAutoApprove.computeIfAbsent(member.bot.id) { ConcurrentHashMap() }
      val deque = groupMap.computeIfAbsent(data.targetGroup!!) {
        ConcurrentLinkedDeque()
      }
      deque.add(AutoApprove(member.id, member.group.id, uid!!))
      launch {
        val receipt = member.group.sendMessage {
          +At(member)
          +" "
          +"""
          成功通过审核~~~ 舰长群号 ${data.targetGroup}，申请后会自动同意。
          【❗重要：入群后先阅读群规，否则后果自负！】如有其他审核问题请联系管理员。
          """.trimIndent()
        }
        if (data.recallDuration != 0L) {
          receipt.recallIn(data.recallDuration * 1000)
        }
      }
    }

    coroutineScope {
      val waitReply: Job?
      var loopJob: Job? = null

      waitReply = launch {
        withTimeoutOrNull(300 * 1000) {
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
            when (validator.validateLoop(member)) {
              ValidatorOperation.PASSED -> {
                whenPassed()
                waitReply.cancel()
                return@launch
              }

              ValidatorOperation.FAILED -> throw CliktError("Validate failed")
              ValidatorOperation.CONTINUED -> {
                // continue
              }
            }
            delay(validator.loopInterval)
          }
        }
      } else {
        null
      }

      listOfNotNull(waitReply, loopJob).joinAll()
    }
  }

  companion object : Entry(
    name = "sign",
    help = "入群验证对话",
    alias = listOf("验证", "驗證"),
  )
}

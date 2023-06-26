package me.hbj.bikkuri.commands

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import me.hbj.bikkuri.command.Command
import me.hbj.bikkuri.command.MiraiCommandSender
import me.hbj.bikkuri.data.Listener
import me.hbj.bikkuri.data.ListenerPersist
import me.hbj.bikkuri.data.TimerTrigger
import me.hbj.bikkuri.data.ValidateMode
import me.hbj.bikkuri.utils.toFriendly
import moe.sdl.yac.core.PrintMessage
import moe.sdl.yac.parameters.options.*
import moe.sdl.yac.parameters.types.int
import moe.sdl.yac.parameters.types.long

class Config(private val sender: MiraiCommandSender) : Command(Config) {
  val member = memberOperator(sender)

  val list by option("--list").flag().help("列出当前配置")
  val enable by option("--on").flag().help("开启监听")
  val disable by option("--off").flag().help("关闭监听")
  val target by option("--target").long().help("目标群号").convert {
    member.bot.getGroup(it) ?: throw PrintMessage("机器人未加入群 $it")
  }
  val bind by option("--bind").long().help("绑定的用户 UID")
  val recall by option("--recall").long().help("群号消息的撤回间隔, 0 为不撤回")
    .validate { it in 0..300 }
  val kick by option("--kick").long().help("超时踢出时长, 单位为秒").validate { it in 0..300 }
  val queue by option("--queue").int().help("同时审批人数").validate { it in 1..10 }
  val mode by option().switch(
    "--recv" to ValidateMode.RECV,
    "--send" to ValidateMode.SEND,
  ).help("验证模式")
  val trigger by option().switch(
    "--on-msg" to TimerTrigger.ON_MSG,
    "--on-join" to TimerTrigger.ON_JOIN,
  ).help("自动踢出刷新模式")

  override suspend fun run(): Unit = coroutineScope cmd@{
    if (enable && disable) throw PrintMessage("无效的输入, 同时选择了开启和关闭监听")

    val groupId = member.group.id
    val map = ListenerPersist.data.listener
    val ref = map.computeIfAbsent(groupId) { Listener() }

    if (list) {
      sender.sendMessage("当前配置为: $ref")
      return@cmd
    }

    val before = ref.copy()
    val msg = StringBuilder()
    if (enable) {
      msg.appendLine("已开启监听器")
      ref.enable = true
    }
    if (disable) {
      msg.appendLine("已关闭监听器")
      ref.enable = false
    }
    target?.also {
      msg.appendLine("将目标群聊设置为: ${target.toFriendly()}")
      ref.targetGroup = it.id
    }
    bind?.also {
      msg.appendLine("将绑定用户设置为: $bind")
      ref.userBind = it
    }
    recall?.also {
      msg.appendLine("将撤回群号时长设置为: $recall")
      ref.recallDuration = it
    }
    kick?.also {
      msg.appendLine("将超时踢出时长设置为: $kick")
      ref.kickDuration = it
    }
    queue?.also {
      msg.appendLine("将同时审核人数设置为: $queue")
      ref.queueSize = it
    }
    mode?.also {
      msg.appendLine("将验证设置为: $mode")
      ref.mode = it
    }
    trigger?.also {
      msg.appendLine("将计时器重置模式设置为: ${it.toFriendly()}")
    }
    if (ref == before) {
      msg.appendLine("配置未变化")
    } else {
      launch { ListenerPersist.save() }
    }
    sender.sendMessage(msg.toString())
  }

  companion object : Entry(
    name = "config",
    help = "机器人审批配置",
  )
}

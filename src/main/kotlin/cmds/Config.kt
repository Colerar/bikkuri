package me.hbj.bikkuri.cmds

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import me.hbj.bikkuri.Bikkuri
import me.hbj.bikkuri.client
import me.hbj.bikkuri.data.GlobalLastMsg
import me.hbj.bikkuri.data.GroupListener
import me.hbj.bikkuri.data.ListenerData
import me.hbj.bikkuri.data.TimerTrigger
import me.hbj.bikkuri.data.ValidateMode
import me.hbj.bikkuri.util.clearIndent
import me.hbj.bikkuri.util.requireOperator
import moe.sdl.yabapi.api.getUserCard
import mu.KotlinLogging
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.MemberCommandSender

private val logger = KotlinLogging.logger {}

object Config :
  CompositeCommand(
    Bikkuri, "config", "配置", "c",
    description = "配置指令"
  ),
  RegisteredCmd {

  @SubCommand
  suspend fun MemberCommandSender.list() {
    requireOperator(this)
    val data = ListenerData.map.getOrPut(group.id) { GroupListener() }
    group.sendMessage("🔍 当前配置: $data")
  }

  @SubCommand
  suspend fun MemberCommandSender.switch() {
    requireOperator(this)
    val id = group.id
    ListenerData.map.getOrPut(id) { GroupListener() }
    val last = ListenerData.map[id]?.enable
    ListenerData.map[id]?.enable = last?.not() ?: true
    val target = ListenerData.map[id]?.targetGroup
    val bind = ListenerData.map[id]?.userBind

    GlobalLastMsg[bot.id].remove(group.id)

    group.sendMessage(
      buildString {
        appendLine("✅ 本群已${if (ListenerData.map[id]?.enable == true) "开启" else "关闭"}监听！")
        if (target == null) appendLine("没有配置审核通过后的目标群聊，可输入 /config target [群号] 配置。")
        if (bind == null) appendLine("没有配置绑定的用户 UID，可输入 /config bind [B站UID] 配置。")
      }.clearIndent()
    )
    logger.debug { "GroupListener[$id] : ${ListenerData.map[id]}" }
  }

  @SubCommand
  suspend fun MemberCommandSender.trigger(trigger: String) {
    requireOperator(this)
    val id = group.id
    ListenerData.map.getOrPut(id) { GroupListener() }
    val last = ListenerData.map[id]?.trigger

    val trigger0 = TimerTrigger.from(trigger) ?: run {
      sendMessage("❌ 输入错误，可用 msg 和 join")
      return
    }

    ListenerData.map[id]?.trigger = trigger0

    group.sendMessage("🔄 计时器重置条件变化： ${last?.toFriendly()} -> ${trigger0.toFriendly()}")
    logger.debug { "GroupListener[$id] : ${ListenerData.map[id]}" }
  }

  private suspend fun getUserInfo(mid: Long?): String {
    return client.getUserCard(mid ?: return "null", false).data?.card?.name?.let {
      "$it(uid$mid)"
    } ?: mid.toString()
  }

  @SubCommand
  suspend fun MemberCommandSender.bind(bind: Long) {
    requireOperator(this)
    val id = group.id
    val data = ListenerData.map.getOrPut(id) { GroupListener(true) }
    val last = data.userBind
    data.userBind = bind
    coroutineScope {
      val lastInfo = async { getUserInfo(last) }
      val info = async { getUserInfo(bind) }
      group.sendMessage("🔄 绑定用户的变化： ${lastInfo.await()} -> ${info.await()}")
    }
    logger.debug { "GroupListener[$id] : ${ListenerData.map[id]}" }
  }

  @SubCommand
  suspend fun MemberCommandSender.target(target: Long) {
    requireOperator(this)
    val id = group.id
    val data = ListenerData.map.getOrPut(id) { GroupListener(true) }
    val last = data.targetGroup
    data.targetGroup = target
    group.sendMessage("🔄 绑定群聊变化： $last -> $target\n记得在目标群聊设置机器人为管理员哦~")
    logger.debug { "GroupListener[$id] : ${ListenerData.map[id]}" }
  }

  @SubCommand
  suspend fun MemberCommandSender.mode(mode: String) {
    requireOperator(this)
    val modeEnum = when (mode.lowercase()) {
      "recv" -> ValidateMode.RECV
      "send" -> ValidateMode.SEND
      else -> {
        group.sendMessage("❌ 输入错误，需要为 RECV 或 SEND。即机器人收消息或机器人发消息。")
        return
      }
    }
    val data = ListenerData.map.getOrPut(group.id) { GroupListener(true) }
    val last = data.mode
    data.mode = modeEnum
    group.sendMessage("🔄 验证模式变化： $last -> $modeEnum")
    logger.debug { "GroupListener[${group.id}] : ${ListenerData.map[group.id]}" }
  }

  @SubCommand("autokick")
  suspend fun MemberCommandSender.autoKick(duration: String) {
    requireOperator(this)
    val id = group.id
    val data = ListenerData.map.getOrPut(id) { GroupListener(true) }
    val last = data.kickDuration
    data.kickDuration = duration.toULongOrNull() ?: run {
      group.sendMessage("❌ 需输入非负整数, 0 代表不自动踢人")
      return
    }
    group.sendMessage("🔄 自动踢人时长变化： $last -> $duration\n注意单位是秒, 0 表示关闭")
    logger.debug { "GroupListener[$id] : ${ListenerData.map[id]}" }
  }
}

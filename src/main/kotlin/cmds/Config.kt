package me.hbj.bikkuri.cmds

import me.hbj.bikkuri.Bikkuri
import me.hbj.bikkuri.Bikkuri.logger
import me.hbj.bikkuri.data.GroupListener
import me.hbj.bikkuri.data.ListenerData
import me.hbj.bikkuri.exception.PermissionForbidden
import me.hbj.bikkuri.util.clearIndent
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.MemberCommandSender
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.utils.debug
import kotlin.contracts.ExperimentalContracts

object Config : CompositeCommand(
  Bikkuri, "config", "配置", "c",
  description = "配置指令"
) {
  @OptIn(ExperimentalContracts::class)
  private fun MemberCommandSender.checkPerm() {
    if (!user.isOperator()) throw PermissionForbidden("/config needs admin perm")
  }

  @SubCommand
  suspend fun MemberCommandSender.list() {
    val data = ListenerData.map.getOrPut(group.id) { GroupListener() }
    group.sendMessage("当前配置: $data")
  }

  @SubCommand
  suspend fun MemberCommandSender.switch() {
    checkPerm()
    val id = group.id
    ListenerData.map.getOrPut(id) { GroupListener() }
    val last = ListenerData.map[id]?.enable
    ListenerData.map[id]?.enable = last?.not() ?: true
    val target = ListenerData.map[id]?.targetGroup
    val bind = ListenerData.map[id]?.userBind

    group.sendMessage(
      buildString {
        appendLine("本群已${if (ListenerData.map[id]?.enable == true) "开启" else "关闭"}监听！")
        if (target == null) appendLine("没有配置审核通过后的目标群聊，输入 /config target [群号] 配置！")
        if (bind == null) appendLine("没有配置绑定的用户 UID，输入 /config bind [B站UID] 配置！")
      }.clearIndent()
    )
    logger.debug { "GroupListener[$id] : ${ListenerData.map[id]}" }
  }

  @SubCommand
  suspend fun MemberCommandSender.bind(bind: Long) {
    checkPerm()
    val id = group.id
    val data = ListenerData.map.getOrPut(id) { GroupListener(true) }
    val last = data.userBind
    data.userBind = bind
    group.sendMessage("绑定用户的 UID 变化： $last -> $bind")
    logger.debug { "GroupListener[$id] : ${ListenerData.map[id]}" }
  }

  @SubCommand
  suspend fun MemberCommandSender.target(target: Long) {
    checkPerm()
    val id = group.id
    val data = ListenerData.map.getOrPut(id) { GroupListener(true) }
    val last = data.targetGroup
    data.targetGroup = target
    group.sendMessage("绑定群聊变化： $last -> $target\n记得在目标群聊设置机器人为管理员哦~")
    logger.debug { "GroupListener[$id] : ${ListenerData.map[id]}" }
  }

  @SubCommand("autokick")
  suspend fun MemberCommandSender.autoKick(duration: String) {
    checkPerm()
    val id = group.id
    val data = ListenerData.map.getOrPut(id) { GroupListener(true) }
    val last = data.kickDuration
    data.kickDuration = duration.toULongOrNull() ?: run {
      group.sendMessage("需输入非负整数, 0 代表不自动踢人")
      return
    }
    group.sendMessage("自动踢人时长变化： $last -> $duration\n注意单位是秒, 0 表示关闭")
    logger.debug { "GroupListener[$id] : ${ListenerData.map[id]}" }
  }
}

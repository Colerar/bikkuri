package me.hbj.bikkuri.events

import kotlinx.coroutines.launch
import me.hbj.bikkuri.Bikkuri.registeredCmds
import me.hbj.bikkuri.cmds.Sign
import me.hbj.bikkuri.data.GlobalLastMsg
import me.hbj.bikkuri.data.ListenerData
import me.hbj.bikkuri.data.TimerTrigger
import me.hbj.bikkuri.tasks.groupsToForward
import me.hbj.bikkuri.util.Forwarder
import me.hbj.bikkuri.util.executeCommandSafely
import me.hbj.bikkuri.util.now
import net.mamoe.mirai.console.command.CommandSender.Companion.asCommandSender
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.content
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

private val allCommandSymbol by lazy {
  (
    registeredCmds.map { it.primaryName } +
      registeredCmds.map { it.secondaryNames.toList() }.flatten()
    ).sorted().toTypedArray()
}

private val cmdRegex by lazy { Regex("""/(\S+)""") }

@OptIn(ExperimentalCommandDescriptors::class, ConsoleExperimentalApi::class)
fun Events.onMessageReceived() {
  filter {
    it is GroupMessageEvent || it is FriendMessageEvent
  }.subscribeAlways<MessageEvent> {
    val content = message.content
    if (content.startsWith("/")) {
      val cmd = cmdRegex.find(content)?.groupValues?.get(1) ?: return@subscribeAlways
      if (allCommandSymbol.binarySearch(cmd) < 0) return@subscribeAlways
      sender.asCommandSender(false).executeCommandSafely(message)
    }
  }
  subscribeAlways<GroupMessageEvent> {
    if (
      sender is NormalMember && // 只用刷新普通成员的上次消息
      ListenerData.isEnabled(group.id) && // 需要开启监听
      ListenerData.map[group.id]?.trigger == TimerTrigger.ON_MSG // 需要有 ON MSG trigger
    ) {
      GlobalLastMsg[bot.id][group.id].map[sender.id] = now()
    }
  }
  subscribeAlways<GroupMessageEvent> {
    if (!ListenerData.isEnabled(group.id)) return@subscribeAlways
    if (it.message.content.matches(Regex("""(["“”]?(开始)?(验证|驗證)["“”]?|^.+/验证$)"""))) {
      (it.sender as? NormalMember)?.asCommandSender(false)?.executeCommandSafely("/${Sign.primaryName}")
    }
  }

  subscribeAlways<GroupMessageEvent> {
    newSuspendedTransaction l@{
      val rel = groupsToForward[it.group.id] ?: return@l
      if (!rel.enabled) return@l
      if (!rel.forwardAll && !rel.forwardees.contains(it.sender.id)) return@l
      if (sender !is NormalMember) return@l
      val showHint = rel.showHint
      rel.toGroups.forEach {
        bot.launch {
          Forwarder.forward(bot.getGroup(it) ?: return@launch, sender as NormalMember, message, showHint)
        }
      }
    }
  }
}

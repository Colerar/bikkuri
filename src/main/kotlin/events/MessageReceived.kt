package me.hbj.bikkuri.events

import me.hbj.bikkuri.Bikkuri.registeredCmds
import me.hbj.bikkuri.cmds.Sign
import me.hbj.bikkuri.data.LastMsg
import me.hbj.bikkuri.data.ListenerData
import me.hbj.bikkuri.util.executeCommandSafely
import mu.KotlinLogging
import net.mamoe.mirai.console.command.CommandSender.Companion.asCommandSender
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.content

private val logger = KotlinLogging.logger {}

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
    if (sender is NormalMember && ListenerData.isEnabled(group.id)) {
      LastMsg.setToNow(group.id, sender.id)
    }
  }
  subscribeAlways<GroupMessageEvent> {
    if (!ListenerData.isEnabled(group.id)) return@subscribeAlways
    if (it.message.content.matches(Regex("""(["“”]?(开始)?(验证|驗證)["“”]?|^.+/验证$)"""))) {
      (it.sender as? NormalMember)?.asCommandSender(false)?.executeCommandSafely(Sign.primaryName)
    }
  }
}

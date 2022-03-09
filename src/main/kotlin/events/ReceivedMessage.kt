package me.hbj.bikkuri.events

import me.hbj.bikkuri.Bikkuri.logger
import me.hbj.bikkuri.Bikkuri.registeredCmds
import me.hbj.bikkuri.data.LastMsg
import me.hbj.bikkuri.exception.PermissionForbidden
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.CommandSender.Companion.asCommandSender
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.EventChannel
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.content

private val allCommandSymbol by lazy {
  (registeredCmds.map { it.primaryName } +
      registeredCmds.map { it.secondaryNames.toList() }.flatten()).sorted().toTypedArray()
}

private val cmdRegex by lazy { Regex("""/(\w+)""") }

@OptIn(ExperimentalCommandDescriptors::class, ConsoleExperimentalApi::class)
fun EventChannel<Event>.onReceivedMessage() {
  filter {
    it is GroupMessageEvent || it is FriendMessageEvent
  }.subscribeAlways<MessageEvent> {
    val content = message.content
    if (content.startsWith("/")) {
      val cmd = cmdRegex.find(content)?.groupValues?.get(1) ?: return@subscribeAlways
      if (allCommandSymbol.binarySearch(cmd) < 0) return@subscribeAlways
      val cmdSender = sender.asCommandSender(false)
      try {
        CommandManager.executeCommand(cmdSender, message, false)
      } catch (e: PermissionForbidden) {
        logger.verbose("Sender permission forbidden ${cmdSender.user.id}", e)
      }
    }
  }
  subscribeAlways<GroupMessageEvent> {
    if (sender is NormalMember) {
      LastMsg.setToNow(group.id, sender.id)
    }
  }
}

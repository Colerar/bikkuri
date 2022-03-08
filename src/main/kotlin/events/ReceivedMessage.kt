package me.hbj.bikkuri.events

import me.hbj.bikkuri.Bikkuri.logger
import me.hbj.bikkuri.exception.PermissionForbidden
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.CommandSender.Companion.asCommandSender
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.EventChannel
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent

@OptIn(ExperimentalCommandDescriptors::class, ConsoleExperimentalApi::class)
fun EventChannel<Event>.onReceivedMessage() {
  filter {
    it is GroupMessageEvent || it is FriendMessageEvent
  }.subscribeAlways<MessageEvent> {
    if (message.contentToString().startsWith("/")) {
      val cmdSender = sender.asCommandSender(false)
      try {
        CommandManager.executeCommand(cmdSender, message, false)
      } catch (e: PermissionForbidden) {
        logger.verbose("Sender permission forbidden ${cmdSender.user.id}", e)
      }
    }
  }
}

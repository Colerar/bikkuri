package me.hbj.bikkuri.command

import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.event.events.MessageEvent

interface CommandSender {
  /**
   * An abstract function to send a message to the sender.
   *
   * @param msg The message to be sent.
   */
  suspend fun sendMessage(msg: String)
}

open class ConsoleCommandSender : CommandSender {
  override suspend fun sendMessage(msg: String): Unit = println(msg)
}

open class MiraiCommandSender(
  val contact: User,
  val event: MessageEvent,
) : CommandSender {
  override suspend fun sendMessage(msg: String) {
    if (contact is Member) {
      contact.group.sendMessage(msg)
    } else {
      contact.sendMessage(msg)
    }
  }
}

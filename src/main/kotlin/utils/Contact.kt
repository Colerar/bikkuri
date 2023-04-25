package me.hbj.bikkuri.utils

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.MessageChainBuilder

suspend inline fun Contact.sendMessage(block: MessageChainBuilder.() -> Unit): MessageReceipt<Contact> {
  val msg = MessageChainBuilder().apply(block).asMessageChain()
  return sendMessage(msg)
}

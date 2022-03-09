package me.hbj.bikkuri.util

import me.hbj.bikkuri.Bikkuri
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.MessageChainBuilder
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource

fun loadImageResource(path: String, formatName: String) =
  Bikkuri.javaClass.classLoader.getResourceAsStream(path)?.toExternalResource(formatName)

suspend fun MessageChainBuilder.addImageOrText(resource: ExternalResource?, contact: Contact) {
  resource?.let { add(contact.uploadImage(it)) } ?: add("[图片]")
}

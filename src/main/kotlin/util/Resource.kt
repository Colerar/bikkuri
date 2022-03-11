package me.hbj.bikkuri.util

import me.hbj.bikkuri.Bikkuri
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.MessageChainBuilder
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import java.io.File

fun loadImageResource(path: String, formatName: String) =
  runCatching {
    Bikkuri.resolveDataFile(path).toExternalResource(formatName)
  }.onFailure {
    Bikkuri.logger.warning("Failed to load image resource in ${File(path).absolutePath}", it)
  }.getOrNull()

suspend fun MessageChainBuilder.addImageOrText(resource: ExternalResource?, contact: Contact) {
  resource?.use {
    add(contact.uploadImage(it))
  } ?: add("[图片]")
}

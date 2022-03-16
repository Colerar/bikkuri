package me.hbj.bikkuri.util

import me.hbj.bikkuri.Bikkuri
import mu.KotlinLogging
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.MessageChainBuilder
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import java.io.File
import java.net.URLDecoder

private val logger = KotlinLogging.logger {}

fun loadImageResource(path: String, formatName: String) =
  runCatching {
    Bikkuri.resolveDataFile(path).toExternalResource(formatName)
  }.onFailure {
    logger.warn(it) { "Failed to load image resource in ${File(path).absolutePath}" }
  }.getOrNull()

suspend fun MessageChainBuilder.addImageOrText(resource: ExternalResource?, contact: Contact) {
  resource?.use {
    add(contact.uploadImage(it))
  } ?: add("[图片]")
}

fun getJarLocation(): File {
  var path: String = Bikkuri::class.java.protectionDomain.codeSource.location.path
  if (System.getProperty("os.name").lowercase().contains("dows")) {
    path = path.substring(1)
  }
  if (path.contains("jar")) {
    path = path.substring(0, path.lastIndexOf("/"))
    return File(URLDecoder.decode(path, Charsets.UTF_8))
  }
  return File(URLDecoder.decode(path.replace("target/classes/", ""), Charsets.UTF_8))
}

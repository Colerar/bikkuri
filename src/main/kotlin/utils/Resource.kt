package me.hbj.bikkuri.utils

import io.github.oshai.kotlinlogging.KotlinLogging
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.MessageChainBuilder
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import java.io.File

private val logger = KotlinLogging.logger {}

/**
 * @param path 图片路径
 * @param formatName 文件拓展名, 若空则尝试从路径推断
 */
fun loadImageResource(path: String, formatName: String? = null): ExternalResource? {
  val format = formatName ?: run {
    when {
      path.contains(".png") -> "png"
      arrayListOf(".jpeg", ".jpg").any { path.contains(it) } -> "jpg"
      else -> null
    }
  }

  return runCatching {
    resolveResourceDirectory(path).toExternalResource(format)
  }.onFailure {
    logger.warn(it) { "Failed to load image resource in ${File(path).absolutePath}" }
  }.getOrNull()
}

suspend fun MessageChainBuilder.addImageOrText(resource: ExternalResource?, contact: Contact) {
  resource?.use {
    add(contact.uploadImage(it))
  } ?: add("[图片]")
}

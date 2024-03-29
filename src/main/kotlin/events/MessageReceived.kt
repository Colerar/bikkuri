package me.hbj.bikkuri.events

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import me.hbj.bikkuri.command.CommandManager
import me.hbj.bikkuri.command.ContextManager
import me.hbj.bikkuri.command.JobIdentity
import me.hbj.bikkuri.command.MiraiCommandSender
import me.hbj.bikkuri.configs.General
import me.hbj.bikkuri.data.ListenerPersist
import me.hbj.bikkuri.utils.byTagFirst
import me.hbj.bikkuri.utils.readToXmlDocument
import me.hbj.bikkuri.utils.sendMessage
import me.hbj.bikkuri.utils.toList
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.utils.MiraiExperimentalApi
import org.w3c.dom.Document

private val logger = KotlinLogging.logger {}

val commandCtxManager = ContextManager()

fun executeCommandSafely(event: MessageEvent, text: String) {
  val command = if (text.startsWith("/")) {
    text.removePrefix("/")
  } else if (text.startsWith(".")) {
    text.removePrefix(".")
  } else {
    return
  }
  val group = (event.sender as? Member)?.group?.id
  val i = JobIdentity(event.bot.id, group, event.sender.id)
  if (command == "cancel") {
    CommandManager.invokeCommand(MiraiCommandSender(event.sender, event), command)
    return
  }
  val begin = commandCtxManager.contextBegin(
    i = i,
    jobInitializer = {
      CommandManager.invokeCommand(MiraiCommandSender(event.sender, event), command)
    },
  )
  if (!begin) {
    logger.debug { "Command job for $i does not begin" }
  }
}

@OptIn(MiraiExperimentalApi::class)
fun Events.onMessageReceived() {
  filter {
    it is GroupMessageEvent || it is FriendMessageEvent
  }.subscribeAlways<MessageEvent> e@{ event ->
    if (sender !is Member && sender !is Friend) return@e
    message.all { it is PlainText || it is At }
    val text = message.content
    executeCommandSafely(event, text)
  }

  subscribeAlways<MessageEvent> e@{
    if (!General.data.biliLightAppConvert) return@e
    val simpleService = message.firstIsInstanceOrNull<SimpleServiceMessage>()
    val videoShort = if (simpleService != null) {
      if (!simpleService.content.contains("b23.tv")) return@e
      val xml = runCatching { simpleService.content.readToXmlDocument() }.getOrNull() ?: return@e
      readBiliVideoOrNull(xml) ?: return@e
    } else {
      val lightApp = message.firstIsInstanceOrNull<LightApp>()?.content ?: return@e
      if (arrayOf("哔哩哔哩", "b23.tv", "bilibili").none(lightApp::contains)) return@e
      val json = Json.decodeFromString<JsonObject>(lightApp)
      val meta = json["meta"]?.jsonObject ?: return@e
      val news = meta["news"]?.jsonObject
      val detail1 = meta["detail_1"]?.jsonObject
      when {
        news != null -> {
          BiliVideo(
            url = news["jumpUrl"]?.jsonPrimitive?.content ?: return@e,
            title = news["title"]?.jsonPrimitive?.content ?: return@e,
          )
        }

        detail1 != null -> {
          BiliVideo(
            url = detail1["qqdocurl"]?.jsonPrimitive?.content ?: return@e,
            title = detail1["desc"]?.jsonPrimitive?.content ?: return@e,
          )
        }

        else -> return@e
      }
    }
    val video = videoShort.toLong() ?: return@e

    val common = "已转换为链接: ${video.url}"
    when (sender) {
      is Member -> {
        (sender as Member).group.sendMessage {
          +source.quote()
          +" $common"
        }
      }

      is Friend -> {
        sender.sendMessage {
          +common
        }
      }
    }
  }

  subscribeAlways<GroupMessageEvent> { event ->
    val enabled = ListenerPersist.listeners.filter { it.value.enable }
    if (!enabled.containsKey(group.id)) return@subscribeAlways
    if (message.content.matches(Regex("""(["“”]?(开始)?(验证|驗證)["“”]?|^.+/验证$)"""))) {
      if (sender !is NormalMember) return@subscribeAlways
      executeCommandSafely(event, "/sign")
    }
  }
}

val clientNoRedirect = HttpClient(OkHttp) {
  followRedirects = false
}

fun readBiliVideoOrNull(document: Document): BiliVideo? {
  val msg = document.byTagFirst("msg") ?: return null
  val item = msg.childNodes.toList().firstOrNull { it.nodeName == "item" } ?: return null
  val title = item.childNodes.toList().firstOrNull { it.nodeName == "title" }?.textContent
  return BiliVideo(
    url = msg.attributes.getNamedItem("url")?.nodeValue ?: return null,
    title = title ?: return null,
  )
}

val A_TAG_REGEX = Regex("<a .+>.+</a>")

data class BiliVideo(
  val url: String,
  val title: String,
) {
  suspend fun toLong(): BiliVideo? = runCatching {
    val url = if (url.contains("b23.tv")) {
      val html = clientNoRedirect.get(url).bodyAsText()
      val aTag = A_TAG_REGEX.find(html)?.value ?: return@runCatching null
      val document = aTag.readToXmlDocument()
      document.byTagFirst("a")?.attributes?.getNamedItem("href")?.nodeValue ?: return@runCatching null
    } else {
      url
    }
    val builder = URLBuilder(url)
    builder.parameters.names().toList() // copy
      .filter { it != "p" && it != "t" }.forEach { builder.parameters.remove(it) }

    if (builder.parameters.names().contains("p") && builder.parameters["p"] == "1") {
      builder.parameters.remove("p")
    }

    this.copy(url = builder.buildString())
  }.getOrNull()
}

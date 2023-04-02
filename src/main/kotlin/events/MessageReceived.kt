package me.hbj.bikkuri.events

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.URLBuilder
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import me.hbj.bikkuri.Bikkuri.registeredCmds
import me.hbj.bikkuri.cmds.Sign
import me.hbj.bikkuri.data.GlobalLastMsg
import me.hbj.bikkuri.data.ListenerData
import me.hbj.bikkuri.data.TimerTrigger
import me.hbj.bikkuri.tasks.groupsToForward
import me.hbj.bikkuri.util.Forwarder
import me.hbj.bikkuri.util.byTagFirst
import me.hbj.bikkuri.util.executeCommandSafely
import me.hbj.bikkuri.util.now
import me.hbj.bikkuri.util.readToXmlDocument
import me.hbj.bikkuri.util.sendMessage
import me.hbj.bikkuri.util.toList
import net.mamoe.mirai.console.command.CommandSender.Companion.asCommandSender
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.LightApp
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.data.SimpleServiceMessage
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.message.data.firstIsInstanceOrNull
import net.mamoe.mirai.utils.MiraiExperimentalApi
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.w3c.dom.Document
import kotlin.collections.set

private val allCommandSymbol by lazy {
  (
    registeredCmds.map { it.primaryName } +
      registeredCmds.map { it.secondaryNames.toList() }.flatten()
    ).sorted().toTypedArray()
}

private val cmdRegex by lazy { Regex("""/(\S+)""") }

@OptIn(ExperimentalCommandDescriptors::class, ConsoleExperimentalApi::class, MiraiExperimentalApi::class)
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

  subscribeAlways<MessageEvent> {
    val simpleService = message.firstIsInstanceOrNull<SimpleServiceMessage>()
    val videoShort = if (simpleService != null) {
      if (!simpleService.content.contains("b23.tv")) return@subscribeAlways
      val xml = runCatching { simpleService.content.readToXmlDocument() }.getOrNull() ?: return@subscribeAlways
      readBiliVideoOrNull(xml) ?: return@subscribeAlways
    } else {
      val lightApp = message.firstIsInstanceOrNull<LightApp>() ?: return@subscribeAlways
      if (!lightApp.content.contains("哔哩哔哩")) return@subscribeAlways
      val json = Json.decodeFromString<JsonObject>(lightApp.content)
      val meta = json["meta"]?.jsonObject ?: return@subscribeAlways
      val news = meta["news"]?.jsonObject
      val detail1 = meta["detail_1"]?.jsonObject
      when {
        news != null -> {
          BiliVideo(
            url = news["jumpUrl"]?.jsonPrimitive?.content ?: return@subscribeAlways,
            title = news["title"]?.jsonPrimitive?.content ?: return@subscribeAlways,
          )
        }

        detail1 != null -> {
          BiliVideo(
            url = detail1["qqdocurl"]?.jsonPrimitive?.content ?: return@subscribeAlways,
            title = detail1["desc"]?.jsonPrimitive?.content ?: return@subscribeAlways,
          )
        }

        else -> return@subscribeAlways
      }
    }
    val video = videoShort.toLong() ?: return@subscribeAlways

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

  subscribeAlways<GroupMessageEvent> {
    if (
      sender is NormalMember && // 只用刷新普通成员的上次消息
      ListenerData.isEnabled(group.id) && // 需要开启监听
      ListenerData.map[group.id]?.trigger == TimerTrigger.ON_MSG // 需要有 ON MSG trigger
    ) {
      GlobalLastMsg[bot.id][group.id].map[sender.id] = now()
    }
  }
  subscribeAlways<GroupMessageEvent> {
    if (!ListenerData.isEnabled(group.id)) return@subscribeAlways
    if (it.message.content.matches(Regex("""(["“”]?(开始)?(验证|驗證)["“”]?|^.+/验证$)"""))) {
      (it.sender as? NormalMember)?.asCommandSender(false)?.executeCommandSafely("/${Sign.primaryName}")
    }
  }

  subscribeAlways<GroupMessageEvent> l@{
    val rel = groupsToForward[it.group.id] ?: return@l
    if (!rel.enabled) return@l
    if (!rel.forwardAll && !rel.forwardees.contains(it.sender.id)) return@l
    if (sender !is NormalMember) return@l
    newSuspendedTransaction {
      rel.toGroups.forEach {
        bot.launch {
          Forwarder.forward(bot.getGroup(it) ?: return@launch, sender as NormalMember, message, rel.showHint)
        }
      }
    }
  }
}

val clientNoRedirect = HttpClient(CIO) {
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
      document.byTagFirst("a")
        ?.attributes
        ?.getNamedItem("href")
        ?.nodeValue
        ?: return@runCatching null
    } else {
      url
    }
    val builder = URLBuilder(url)
    builder.parameters.names().toList() // copy
      .filter { it != "p" && it != "t" }
      .forEach { builder.parameters.remove(it) }

    if (builder.parameters.names().contains("p") && builder.parameters["p"] == "1") {
      builder.parameters.remove("p")
    }

    this.copy(url = builder.buildString())
  }.getOrNull()
}

package me.hbj.bikkuri.tasks

import kotlinx.coroutines.*
import me.hbj.bikkuri.data.ListenerPersist
import me.hbj.bikkuri.events.queuedMemberRequest
import mu.KotlinLogging
import net.mamoe.mirai.contact.getMember
import net.mamoe.mirai.contact.isOperator

private val logger = KotlinLogging.logger {}

fun CoroutineScope.launchAutoApproveTask(): Job = launch {
  while (isActive) {
    delay(5_000)
    if (queuedMemberRequest.isEmpty()) continue
    ListenerPersist.data.listener.asSequence()
      .filter { (_, v) -> v.enable }
      .forEach { (groupId, listener) ->
        val e = queuedMemberRequest
          .firstOrNull { it.groupId == groupId } ?: return@forEach

        // Bot must be admin
        if (e.group?.getMember(e.bot.id)?.isOperator() == false) return@forEach

        // Requires total - operator < queue
        val count = e.group?.members?.count { !it.isOperator() } ?: return@forEach
        if (count >= listener.queueSize) return@forEach

        e.accept()

        logger.info { "Accept join request: $e" }

        queuedMemberRequest.remove(e)
      }
  }
}

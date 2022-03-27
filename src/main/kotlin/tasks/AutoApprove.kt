package me.hbj.bikkuri.tasks

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.hbj.bikkuri.data.General
import me.hbj.bikkuri.data.ListenerData
import me.hbj.bikkuri.events.queuedMemberRequest
import mu.KotlinLogging
import net.mamoe.mirai.contact.getMember
import net.mamoe.mirai.contact.isOperator

private val logger = KotlinLogging.logger {}

fun CoroutineScope.launchAutoApproveTask(): Job = launch {
  while (isActive) {
    delay(General.time.autoApprove)
    ListenerData.map
      .filter { it.value.enable }
      .forEach { (groupId, _) ->
        val e = queuedMemberRequest
          .firstOrNull { it.groupId == groupId } ?: return@forEach

        // Bot must be admin
        if (e.group?.getMember(e.bot.id)?.isOperator() == false) return@forEach

        // keep only one in group
        if (e.group?.members?.count { !it.isOperator() } != 0) return@forEach

        e.accept()

        logger.info { "Accept join request: $e" }

        queuedMemberRequest.remove(e)
      }
  }
}

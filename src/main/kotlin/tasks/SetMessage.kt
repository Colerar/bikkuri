package me.hbj.bikkuri.tasks

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import me.hbj.bikkuri.bili.api.modifyMessageSetting
import me.hbj.bikkuri.bili.data.GeneralCode
import me.hbj.bikkuri.client

private val logger = io.github.oshai.kotlinlogging.KotlinLogging.logger { }

fun CoroutineScope.setMessageTask(): Job = launch {
  logger.info { "Modifying message setting" }
  runCatching {
    client.modifyMessageSetting {
      Intercept set off
      FoldUnfollowed set off
    }.also {
      if (it.code != GeneralCode.SUCCESS) logger.warn { "Failed to modify message setting: $it" }
    }
  }.onFailure {
    if (it is IllegalArgumentException) {
      logger.warn(it) { "Failed to modify message setting, may be not login" }
    }
  }
}

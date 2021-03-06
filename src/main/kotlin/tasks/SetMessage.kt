package me.hbj.bikkuri.tasks

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import me.hbj.bikkuri.Bikkuri
import me.hbj.bikkuri.client
import moe.sdl.yabapi.api.modifyMessageSetting
import moe.sdl.yabapi.data.GeneralCode
import net.mamoe.mirai.utils.warning

private val logger = mu.KotlinLogging.logger { }

fun CoroutineScope.setMessageTask(): Job = launch {
  logger.info { "Modifying message setting" }
  runCatching {
    client.modifyMessageSetting {
      Intercept set off
      FoldUnfollowed set off
    }.also {
      if (it.code != GeneralCode.SUCCESS) Bikkuri.logger.warning { "Failed to modify message setting: $it" }
    }
  }.onFailure {
    if (it is IllegalArgumentException) {
      logger.warn(it) { "Failed to modify message setting, may be not login" }
    }
  }
}

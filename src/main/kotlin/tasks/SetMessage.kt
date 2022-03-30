package me.hbj.bikkuri.tasks

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import me.hbj.bikkuri.Bikkuri
import me.hbj.bikkuri.client
import moe.sdl.yabapi.api.modifyMessageSetting
import moe.sdl.yabapi.data.GeneralCode
import net.mamoe.mirai.utils.info
import net.mamoe.mirai.utils.warning

fun CoroutineScope.setMessageTask(): Job = launch {
  Bikkuri.logger.info { "Modifying message setting" }
  client.modifyMessageSetting {
    Intercept set off
    FoldUnfollowed set off
  }.also {
    if (it.code != GeneralCode.SUCCESS) Bikkuri.logger.warning { "Failed to modify message setting: $it" }
  }
}

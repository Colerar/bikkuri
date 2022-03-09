package me.hbj.bikkuri.cmds

import me.hbj.bikkuri.Bikkuri
import me.hbj.bikkuri.config.BUILD_TIME
import me.hbj.bikkuri.config.MIRAI_VERSION
import me.hbj.bikkuri.config.VERSION
import me.hbj.bikkuri.util.clearIndent
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.SimpleCommand

object Version : SimpleCommand(
  Bikkuri, "version", "版本", "v",
  description = "查看版本信息"
) {
  @Handler
  suspend fun CommandSender.handle() {
    sendMessage(
      """
            Bikkuri Q群机器人 - $VERSION
            ⏱ Built at $BUILD_TIME
            ❤️ With Kotlin ${KotlinVersion.CURRENT} & Mirai $MIRAI_VERSION
        """.clearIndent()
    )
  }
}

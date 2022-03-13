package me.hbj.bikkuri.cmds

import me.hbj.bikkuri.Bikkuri
import me.hbj.bikkuri.client
import moe.sdl.yabapi.api.getBasicInfo
import moe.sdl.yabapi.api.logOut
import moe.sdl.yabapi.api.loginWebQRCodeInteractive
import net.mamoe.mirai.console.command.ConsoleCommandSender
import net.mamoe.mirai.console.command.SimpleCommand

object LoginBili : SimpleCommand(
  Bikkuri, "loginbili",
  description = "登录 B 站帐号"
) {
  @Handler
  suspend fun @Suppress("unused") ConsoleCommandSender.login() {
    if (client.getBasicInfo().data.isLogin) {
      client.logOut()
    }
    client.loginWebQRCodeInteractive {
      Bikkuri.logger.info(it)
    }
  }
}

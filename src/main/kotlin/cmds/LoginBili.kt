package me.hbj.bikkuri.cmds

import me.hbj.bikkuri.Bikkuri
import me.hbj.bikkuri.client
import moe.sdl.yabapi.api.getBasicInfo
import moe.sdl.yabapi.api.logOut
import moe.sdl.yabapi.api.loginWebConsole
import moe.sdl.yabapi.api.loginWebQRCodeInteractive
import moe.sdl.yabapi.api.loginWebSMSConsole
import mu.KotlinLogging
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.ConsoleCommandSender

private val logger = KotlinLogging.logger {}

@Suppress("unused")
object LoginBili : CompositeCommand(
  Bikkuri, "loginbili", "bililogin",
  description = "登录 B 站帐号"
) {
  suspend fun logOutIfLogin() {
    if (client.getBasicInfo().data.isLogin)
      client.logOut()
  }

  @SubCommand("qr", "qrcode")
  @Description("通过二维码登录B站帐号")
  suspend fun ConsoleCommandSender.qr() {
    logOutIfLogin()
    client.loginWebQRCodeInteractive { Bikkuri.logger.info(it) }
    Bikkuri.logger.info("Logged in successfully via qrcode")
  }

  @SubCommand("sms")
  @Description("通过短信登录B站帐号")
  suspend fun ConsoleCommandSender.sms() {
    logOutIfLogin()
    client.loginWebSMSConsole(true) { Bikkuri.logger.info(it) }
    Bikkuri.logger.info("Logged in successfully via sms")
  }

  @SubCommand("pwd", "password")
  @Description("通过密碼登录B站帐号")
  suspend fun ConsoleCommandSender.pwd() {
    logOutIfLogin()
    client.loginWebConsole { Bikkuri.logger.info(it) }
    Bikkuri.logger.info("Logged in successfully via pwd")
  }
}

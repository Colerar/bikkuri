package me.hbj.bikkuri.cmds

import me.hbj.bikkuri.Bikkuri
import me.hbj.bikkuri.client
import moe.sdl.yabapi.api.getBasicInfo
import moe.sdl.yabapi.api.logOut
import moe.sdl.yabapi.api.loginWebConsole
import moe.sdl.yabapi.api.loginWebQRCodeInteractive
import moe.sdl.yabapi.api.loginWebSMSConsole
import moe.sdl.yabapi.data.GeneralCode
import moe.sdl.yabapi.data.login.LoginWebSMSResponseCode
import mu.KotlinLogging
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.ConsoleCommandSender

private val logger = KotlinLogging.logger {}

@Suppress("unused")
object LoginBili : CompositeCommand(
  Bikkuri, "loginbili", "bililogin",
  description = "登录 B 站帐号"
) {
  private suspend fun logOutIfLogin() {
    if (client.getBasicInfo().data.isLogin)
      client.logOut()
  }

  @SubCommand("qr", "qrcode")
  @Description("通过二维码登录B站帐号")
  suspend fun ConsoleCommandSender.qr() {
    logOutIfLogin()
    val resp = client.loginWebQRCodeInteractive { Bikkuri.logger.info(it) }.lastOrNull()
    if (resp?.code == GeneralCode.SUCCESS) {
      logger.info("Logged in successfully via qrcode")
    } else logger.warn("Failed to login")
  }

  @SubCommand("sms")
  @Description("通过短信登录B站帐号")
  suspend fun ConsoleCommandSender.sms() {
    logOutIfLogin()
    val resp = client.loginWebSMSConsole(true) { Bikkuri.logger.info(it) }
    if (resp.code == LoginWebSMSResponseCode.SUCCESS) {
      logger.info("Logged in successfully via sms")
    } else logger.warn("Failed to login")
  }

  @SubCommand("pwd", "password")
  @Description("通过密碼登录B站帐号")
  suspend fun ConsoleCommandSender.pwd() {
    logOutIfLogin()
    client.loginWebConsole { Bikkuri.logger.info(it) }
    logger.info("Logged in successfully via pwd")
  }
}

package me.hbj.bikkuri.cmds

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import me.hbj.bikkuri.Bikkuri
import me.hbj.bikkuri.client
import moe.sdl.yabapi.BiliClient
import moe.sdl.yabapi.api.getBasicInfo
import moe.sdl.yabapi.api.getWebQRCode
import moe.sdl.yabapi.api.logOut
import moe.sdl.yabapi.api.loginWebConsole
import moe.sdl.yabapi.api.loginWebQRCode
import moe.sdl.yabapi.api.loginWebSMSConsole
import moe.sdl.yabapi.data.GeneralCode
import moe.sdl.yabapi.data.login.LoginWebQRCodeResponse
import moe.sdl.yabapi.data.login.LoginWebQRCodeResponseCode
import moe.sdl.yabapi.data.login.LoginWebSMSResponseCode
import mu.KotlinLogging
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.ConsoleCommandSender
import kotlin.coroutines.CoroutineContext

private val logger = KotlinLogging.logger {}

@Suppress("unused")
object LoginBili :
  CompositeCommand(
    Bikkuri, "loginbili", "bililogin",
    description = "登录 B 站帐号"
  ),
  RegisteredCmd {
  private suspend fun logOutIfLogin() {
    if (client.getBasicInfo().data.isLogin)
      client.logOut()
  }

  @SubCommand("qr", "qrcode")
  @Description("通过二维码登录B站帐号")
  suspend fun ConsoleCommandSender.qr() {
    logOutIfLogin()
    val resp = client.loginQr().lastOrNull()
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

suspend fun BiliClient.loginQr(
  context: CoroutineContext = this.context,
): List<LoginWebQRCodeResponse> =
  withContext(context) {
    logger.debug { "Starting Interactive Login via Web QR Code" }
    val getQrResponse = getWebQRCode()
    logger.info { "打开网站，通过Bilibili手机客户端扫描二维码。" }
    logger.info { getQrResponse.data?.url }
    val loop = atomic(true)
    val responseList = mutableListOf<LoginWebQRCodeResponse>()
    withTimeoutOrNull(120_000) {
      do {
        loginWebQRCode(getQrResponse).also {
          responseList.add(it)
          require((it.dataWhenSuccess == null) xor (it.dataWhenFailed == null)) {
            "Invalid Response"
          }
          if (it.dataWhenSuccess != null) {
            loop.getAndSet(false)
            logger.info { "Logged in successfully!" }
            return@also
          }
          when (it.dataWhenFailed) {
            LoginWebQRCodeResponseCode.NOT_SCAN -> logger.trace { "wait for QR code scanning" }
            LoginWebQRCodeResponseCode.NOT_CONFIRM -> logger.trace { "wait for confirming" }
            LoginWebQRCodeResponseCode.KEY_EXPIRED -> cancel("QRCode Time Out")
            else -> throw IllegalStateException("unexpected code, ${it.dataWhenFailed}")
          }
        }
        delay(1_000)
      } while (loop.value)
    }
    responseList
  }

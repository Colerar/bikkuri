package me.hbj.bikkuri.commands

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import me.hbj.bikkuri.bili.BiliClient
import me.hbj.bikkuri.bili.api.*
import me.hbj.bikkuri.bili.data.GeneralCode
import me.hbj.bikkuri.bili.data.login.LoginWebQRCodeResponse
import me.hbj.bikkuri.bili.data.login.LoginWebQRCodeResponseCode
import me.hbj.bikkuri.bili.data.login.LoginWebSMSResponseCode
import me.hbj.bikkuri.client
import me.hbj.bikkuri.command.Command
import me.hbj.bikkuri.commands.LoginType.*
import moe.sdl.yac.parameters.options.default
import moe.sdl.yac.parameters.options.option
import moe.sdl.yac.parameters.options.switch
import java.net.URLEncoder
import kotlin.coroutines.CoroutineContext

private val logger = KotlinLogging.logger {}

private suspend fun logOutIfLogin() {
  if (client.getBasicInfo().data.isLogin) {
    client.logOut()
  }
}

enum class LoginType {
  QR, SMS, PASSWORD,
}

class LoginBili : Command(LoginBili) {
  private val way by option().switch(
    "--qr" to QR,
    "--sms" to SMS,
    "--password" to PASSWORD,
  ).default(QR)

  override suspend fun run() {
    logOutIfLogin()
    when (way) {
      QR -> {
        val resp = client.loginQr().lastOrNull()
        if (resp?.code == GeneralCode.SUCCESS) {
          logger.info { "Logged in successfully via qrcode" }
        } else {
          logger.warn { "Failed to login" }
        }
      }

      SMS -> {
        val resp = client.loginWebSMSConsole(true) {
          logger.info { it }
        }
        if (resp.code == LoginWebSMSResponseCode.SUCCESS) {
          logger.info { "Logged in successfully via sms" }
        } else {
          logger.warn { "Failed to login" }
        }
      }

      PASSWORD -> {
        client.loginWebConsole {
          logger.info { it }
        }
        logger.info { "Logged in successfully via pwd" }
      }
    }
  }

  companion object : Entry(
    name = "loginbili",
    help = "登录 B 站帐号",
  )
}

suspend fun BiliClient.loginQr(
  context: CoroutineContext = this.context,
): List<LoginWebQRCodeResponse> =
  withContext(context) {
    logger.debug { "Starting Interactive Login via Web QR Code" }
    val getQrResponse = getWebQRCode()
    logger.info { "打开网站，通过 Bilibili 手机客户端扫描二维码。" }
    val urlencoded = URLEncoder.encode(getQrResponse.data?.url, Charsets.UTF_8)
    logger.info { "https://qr.sdl.moe?text=$urlencoded" }
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

package me.hbj.bikkuri.bili.api

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import me.hbj.bikkuri.bili.BiliClient
import me.hbj.bikkuri.bili.consts.internal.*
import me.hbj.bikkuri.bili.data.GeneralCode
import me.hbj.bikkuri.bili.data.login.*
import me.hbj.bikkuri.bili.data.login.LoginWebQRCodeResponseCode.*
import me.hbj.bikkuri.bili.deserializeJson
import me.hbj.bikkuri.bili.util.encoding.RSAProvider
import me.hbj.bikkuri.bili.util.encoding.trimPem
import me.hbj.bikkuri.bili.util.requireCmdInputNumber
import me.hbj.bikkuri.bili.util.requireCmdInputString
import kotlin.coroutines.CoroutineContext

private val logger = KotlinLogging.logger { }

private suspend fun BiliClient.isLogin(): Boolean = getBasicInfo().data.isLogin

private suspend fun BiliClient.needLogin() {
  if (!isLogin()) throw IllegalStateException("You need login first!")
}

private suspend fun BiliClient.noNeedLogin() {
  if (isLogin()) throw IllegalStateException("You are already logged in!")
}

/**
 * 获取人机验证码, 需要手动验证
 *
 * 可使用 [https://kuresaru.github.io/geetest-validator/]
 * @return [GetCaptchaResponse]
 */
suspend fun BiliClient.getCaptcha(
  source: String = "main_web",
  context: CoroutineContext = this.context,
): GetCaptchaResponse = withContext(context) {
  logger.info { "Querying Login Captcha" }
  client.get(QUERY_CAPTCHA_URL) {
    parameter("source", source)
  }
    .body<String>()
    .deserializeJson<GetCaptchaResponse>()
    .also { logger.debug { "Query Captcha Response: $it" } }
}

/**
 * 通过 Web 方式获取 RSA 公钥
 * @return [RsaGetResponse]
 */
suspend fun BiliClient.getRsaKeyWeb(): RsaGetResponse = withContext(context) {
  logger.info { "Getting RSA Key by Web method" }
  client.get(RSA_GET_WEB_URL) {
    parameter("act", "getkey")
  }.body<String>().deserializeJson<RsaGetResponse>().also {
    logger.debug { "RSA Get Web Response: $it" }
  }
}

/**
 *  通过 Web 方式登录, 流程为 [getCaptcha] -> [getRsaKeyWeb] -> [loginWeb]
 *
 *  @param userName B站帐号, 手机号或邮箱
 *  @param pwdEncrypted 密码, 输入 rsa(pubkey, salt+password) 得到的密文
 *  @param validate 验证码平台返回
 *  @param seccode 验证码平台返回
 *  @param getCaptchaResponse B 站验证码请求回调
 */
@Suppress("LongParameterList")
suspend fun BiliClient.loginWeb(
  userName: String,
  pwdEncrypted: String,
  validate: String,
  seccode: String,
  getCaptchaResponse: GetCaptchaResponse,
  source: String = "main_web",
  context: CoroutineContext = this.context,
): LoginWebResponse = withContext(context) {
  noNeedLogin()
  logger.info { "Logging in by Web method" }
  client.post(LOGIN_WEB_URL) {
    val params = Parameters.build {
      append("source", source)
      append("username", userName)
      append("password", pwdEncrypted)
      append("keep", "true")
      append("token", getCaptchaResponse.data?.token ?: error("Failed to get login token"))
      append("challenge", getCaptchaResponse.data.geetest?.challenge ?: error("Failed to get geetest challenge"))
      append("validate", validate)
      append("seccode", seccode)
    }
    setBody(FormDataContent(params))
  }.body<String>().deserializeJson<LoginWebResponse>().also {
    logger.debug { "Login Web Response: $it" }
    if (it.code != LoginWebResponseCode.SUCCESS) {
      logger.warn { "Login failed error code ${it.code}, with message: ${it.message}" }
    }
  }
}

/**
 * 命令行交互式帳號密碼登錄
 */
suspend fun BiliClient.loginWebConsole(
  encryptFunc: (String, RsaGetResponse) -> String = { pwd, response ->
    if (response.rsa != null) {
      RSAProvider.encryptWithPublicKey(trimPem(response.rsa), response.salt + pwd)
    } else {
      error("Failed to encrypt data, RSA key is null")
    }
  },
  context: CoroutineContext = this.context,
  out: (String) -> Unit = ::println,
): Unit = withContext(context) {
  noNeedLogin()
  logger.info { "Starting Console Interactive Bilibili Web Login" }
  val userName = requireCmdInputString("Please Input Bilibili Username:", outFunc = out)
  val pwd = requireCmdInputString("Please Input Bilibili Password:", outFunc = out)
  val captchaResponse = getCaptcha()
  out("Please prove you are human, do the captcha via https://kuresaru.github.io/geetest-validator/ :")
  out("gt=${captchaResponse.data?.geetest?.gt}, challenge=${captchaResponse.data?.geetest?.challenge}")
  val validate = requireCmdInputString("input the result, validate=", outFunc = out)
  val seccode = "$validate|jordan"
  val encryptPwd = encryptFunc(pwd, this@loginWebConsole.getRsaKeyWeb())
  loginWeb(userName, encryptPwd, validate, seccode, captchaResponse)
}

suspend fun BiliClient.getWebQRCode(
  context: CoroutineContext = this.context,
): QRCodeWebGetResponse = withContext(context) {
  logger.info { "Getting Web QRCode" }
  client.get(LOGIN_QRCODE_GET_WEB_URL)
    .body<String>()
    .deserializeJson<QRCodeWebGetResponse>()
    .also {
      logger.debug { "QRCode Web Get Response: $it" }
      if (it.code != GeneralCode.SUCCESS) logger.warn { "QRCode Web Get failed, error code: ${it.code}" }
    }
}

suspend fun BiliClient.loginWebQRCode(
  qrResponse: QRCodeWebGetResponse,
  context: CoroutineContext = this.context,
): LoginWebQRCodeResponse = withContext(context) {
  noNeedLogin()
  logger.debug { "Starting Logging in via Web QR Code" }
  client.post(LOGIN_WEB_QRCODE_URL) {
    val params = Parameters.build {
      append("oauthKey", qrResponse.data?.oauthKey ?: error("Failed to get oauthKey"))
    }
    setBody(FormDataContent(params))
  }.body<String>().deserializeJson<LoginWebQRCodeResponse>().also {
    logger.debug { "Login Web QR Code Response: $it" }
    if (it.code != GeneralCode.SUCCESS) logger.debug { "Login Web via QR Code failed, error code: ${it.code}" }
  }
}

/**
 * 交互式扫码登录封装
 */
suspend fun BiliClient.loginWebQRCodeInteractive(
  context: CoroutineContext = this.context,
  out: (String) -> Unit = ::println,
): List<LoginWebQRCodeResponse> =
  withContext(context) {
    noNeedLogin()
    logger.debug { "Starting Interactive Login via Web QR Code" }
    val getQrResponse = getWebQRCode()
    out("打开网站，通过Bilibili手机客户端扫描二维码。")
    out("https://qrcode.jp/qr?q=${getQrResponse.data?.url}&s=10")
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
            NOT_SCAN -> logger.trace { "wait for QR code scanning" }
            NOT_CONFIRM -> logger.trace { "wait for confirming" }
            KEY_EXPIRED -> cancel("QRCode Time Out")
            else -> throw IllegalStateException("unexpected code, ${it.dataWhenFailed}")
          }
        }
        delay(1_000)
      } while (loop.value)
    }
    responseList
  }

/**
 * 获取国际区码
 * @return [CallingCodeGetResponse]
 */
suspend fun BiliClient.getCallingCode(
  context: CoroutineContext = this.context,
): CallingCodeGetResponse = withContext(context) {
  logger.info { "Getting Calling Code" }
  client.get(GET_CALLING_CODE_URL)
    .body<String>()
    .deserializeJson<CallingCodeGetResponse>()
    .also {
      logger.debug { "Calling Code Get Response: $it" }
      if (it.code != GeneralCode.SUCCESS) logger.warn { "Calling Code Get failed, error code: ${it.code}" }
    }
}

/**
 * 請求短信驗證碼
 * @param phone 手機號
 * @param cid 區域代碼 可通過 [getCallingCode] 获取
 * @param captchaResponse [GetCaptchaResponse]
 * @param validate 驗證碼結果
 * @param seccode 驗證碼結果
 */
suspend fun BiliClient.requestSMSCode(
  phone: Long,
  cid: Int,
  captchaResponse: GetCaptchaResponse,
  validate: String,
  seccode: String,
  source: String = "main_web",
  context: CoroutineContext = this.context,
): SendSMSResponse = withContext(context) {
  logger.info { "Requesting SMS Code" }
  client.post(SEND_SMS_URL) {
    val params = Parameters.build {
      append("tel", phone.toString())
      append("cid", cid.toString())
      append("source", source)
      append("token", captchaResponse.data?.token ?: error("failed to get login token"))
      append("challenge", captchaResponse.data.geetest?.challenge ?: error("failed to get geetest challenge"))
      append("validate", validate)
      append("seccode", seccode)
    }
    setBody(FormDataContent(params))
  }.body<String>().deserializeJson<SendSMSResponse>().also {
    logger.debug { "SMS Code Request Response: $it" }
    if (it.code != SendSMSResponseCode.SUCCESS) {
      logger.warn { "SMS Code Request failed, error code: ${it.code}" }
    }
  }
}

/**
 * 通過短信驗證碼登錄, 流程爲 [getCaptcha] -> [requestSMSCode] -> [loginWebSMS]
 * @param phone 手機號
 * @param cid 區域代碼 可通過 [getCallingCode] 获取
 * @param code 短信驗證碼
 * @param sendSMSResponse [SendSMSResponse]
 */
suspend fun BiliClient.loginWebSMS(
  phone: Long,
  cid: Int,
  code: Int,
  sendSMSResponse: SendSMSResponse,
  source: String = "main_web",
  context: CoroutineContext = this.context,
): LoginWebSMSResponse = withContext(context) {
  logger.info { "Logging in via Web SMS" }
  client.post(LOGIN_WEB_SMS_URL) {
    val smsCaptchaKey = sendSMSResponse.captchaKey ?: throw IllegalArgumentException("Captcha key is null")
    val params = Parameters.build {
      append("tel", phone.toString())
      append("cid", cid.toString())
      append("code", code.toString())
      append("source", source)
      append("captcha_key", smsCaptchaKey)
    }
    setBody(FormDataContent(params))
  }.body<String>().deserializeJson<LoginWebSMSResponse>().also {
    logger.debug { "Login Web SMS Response: $it" }
    if (it.code != LoginWebSMSResponseCode.SUCCESS) logger.warn { "Login Web SMS failed, error code: ${it.code}" }
  }
}

/**
 * 命令行交互式短信驗證登錄
 * @param needsCallingCode 是否需要國際區碼, 默認 +86
 */
suspend fun BiliClient.loginWebSMSConsole(
  needsCallingCode: Boolean = false,
  context: CoroutineContext = this.context,
  out: (String) -> Unit = ::println,
): LoginWebSMSResponse = withContext(context) {
  noNeedLogin()
  logger.info { "Starting Console Interactive Bilibili Web Login" }
  val countryCode =
    if (needsCallingCode) requireCmdInputNumber("Please Input Country Code(e.g, 86, 1)", outFunc = out) else 86

  val phone: Long = requireCmdInputNumber("Please Input Phone Number (e.g. 13800138000):", outFunc = out)

  var smsSent = false
  var sendSMSResponse: SendSMSResponse? = null
  while (!smsSent) {
    val captchaResponse = getCaptcha()
    out("Please do the captcha via https://kuresaru.github.io/geetest-validator/ :")
    out("gt=${captchaResponse.data?.geetest?.gt}, challenge=${captchaResponse.data?.geetest?.challenge}")
    val validate = requireCmdInputString("validate=", outFunc = out)
    sendSMSResponse = requestSMSCode(phone, countryCode, captchaResponse, validate, "$validate|jordan")
    if (sendSMSResponse.code == SendSMSResponseCode.SUCCESS) {
      out("SMS Code Sent")
      smsSent = true
    } else {
      out("SMS Code Request Failed, Error Code: ${sendSMSResponse.code}")
      out("Please Retry!")
    }
  }
  requireNotNull(sendSMSResponse) { "SMS Code Request Failed" }
  val code: Int = requireCmdInputNumber("Please Input SMS Code (e.g. 123456):", outFunc = out)
  loginWebSMS(phone, countryCode, code, sendSMSResponse)
}

suspend fun BiliClient.logOut(
  context: CoroutineContext = this.context,
): LogOutResponse = withContext(context) {
  logger.info { "Logging out" }
  needLogin()
  client.post(LOG_OUT_URL) {
    val params = Parameters.build {
      putCsrf("biliCSRF")
    }
    setBody(FormDataContent(params))
  }.body<String>().deserializeJson<LogOutResponse>().also {
    logger.debug { "Log Out Response: $it" }
    if (it.code != GeneralCode.SUCCESS) logger.warn { "Log Out failed, error code: ${it.code}" }
  }
}

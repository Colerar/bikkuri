@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package me.hbj.bikkuri

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import me.hbj.bikkuri.command.CommandManager
import me.hbj.bikkuri.command.ConsoleCommandSender
import me.hbj.bikkuri.commands.defaultsCommand
import me.hbj.bikkuri.config.BUILD_BRANCH
import me.hbj.bikkuri.config.COMMIT_HASH
import me.hbj.bikkuri.config.VERSION
import me.hbj.bikkuri.configs.AutoLoginConfig
import me.hbj.bikkuri.configs.AutoLoginConfig.Auth.*
import me.hbj.bikkuri.configs.General
import me.hbj.bikkuri.data.BackupTaskPersist
import me.hbj.bikkuri.data.ListenerPersist
import me.hbj.bikkuri.db.*
import me.hbj.bikkuri.events.*
import me.hbj.bikkuri.persist.DatabaseManager
import me.hbj.bikkuri.tasks.*
import me.hbj.bikkuri.utils.ModuleScope
import me.hbj.bikkuri.utils.absPath
import me.hbj.bikkuri.utils.globalWorkDirectory
import me.hbj.bikkuri.utils.lazyUnsafe
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.auth.BotAuthorization
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.internal.utils.MiraiProtocolInternal
import top.mrxiaom.qsign.QSignService
import java.io.File
import java.util.*
import kotlin.LazyThreadSafetyMode.NONE
import kotlin.system.exitProcess
import kotlin.system.measureTimeMillis

val logger by lazy(NONE) { KotlinLogging.logger {} }

val configs = listOf(AutoLoginConfig, ListenerPersist, BackupTaskPersist, General)

val dbs = listOf(
  ApproveLink,
  BiliBlocklist,
  Blocklist,
  BlocklistLink,
  BotAccepted,
  DupAllow,
  JoinTimes,
  GuardList,
  GuardLastUpdate,
  ForwardTable,
  ForwardToGroupSet,
  ForwardeeSet,
)

fun main(): Unit = runBlocking {
  logger.info { "Starting Bikkuri ${versionFormatted()}" }
  logger.info { "Working directory: $globalWorkDirectory" }

  System.setProperty("mirai.no-desktop", "true")

  val bikkuriScope = ModuleScope("Bikkuri", this.coroutineContext)

  val loadConfigs = bikkuriScope.launch {
    configs.map {
      launch {
        it.initAndLoad()
      }
    }
  }

  val loadCommands = bikkuriScope.launch {
    val ms = measureTimeMillis {
      CommandManager.registerCommands(defaultsCommand)
    }
    val names = defaultsCommand.map { it.entry.name }
    logger.info { "Costed $ms ms to init command, total ${names.size}: ${names.joinToString()}" }
  }

  joinAll(loadConfigs, loadCommands)

  logger.info { "useQSign = ${AutoLoginConfig.data.useQSign}" }
  if (AutoLoginConfig.data.useQSign) {
    val path =
      AutoLoginConfig.data.txlibPath?.let { File(it) }
        ?: globalWorkDirectory.resolve("txlib").apply { mkdirs() }
    require(path.exists() && path.isDirectory) { "txlibPath should be a directory and exist" }
    val version = AutoLoginConfig.data.txlibVersion ?: "8.9.63"
    val txlib = path.resolve(version)
    require(txlib.exists() && path.isDirectory) { "Cannot found ${txlib.toPath()}, is version `$version` exists?" }

    QSignService.Factory.apply {
      init(txlib)
      loadProtocols()
      register()
    }
  }

  GlobalEventChannel.apply {
    onBotOnline()
    onBotOffline()
    onMemberJoin()
    onMemberRequest()
    onMemberLeave()
    onMessageReceived()
  }

  val tasksModule = ModuleScope("Tasks", bikkuriScope.coroutineContext)
  tasksModule.apply {
    launchAutoApproveTask()
    launchAutoBackupTask()
    launchAutoKickTask()
    launchUpdateGuardListTask()
    setupBiliClient()
  }

  val loginBots = bikkuriScope.launch {
    loginBots()
  }

  val loadDatabase = bikkuriScope.launch {
    val ms = measureTimeMillis {
      DatabaseManager.loadDatabase()
      DatabaseManager.loadTables(dbs)
    }
    logger.info { "Costed $ms ms to load databases" }
  }

  joinAll(loginBots, loadDatabase)

  setupConsole()
}

suspend fun setupConsole() = withContext(Dispatchers.IO) {
  var args: String?
  while (true) {
    args = readln()
    CommandManager.invokeCommand(ConsoleCommandSender(), args)
  }
}

suspend fun loginBots() = withContext(Dispatchers.IO) {
  if (AutoLoginConfig.data.accounts.isEmpty()) {
    logger.info {
      "当前自动登录字段为空, 请于 ${AutoLoginConfig.file.absPath} 处修改配置文件, 示例如下:\n" +
        AutoLoginConfig.exampleToString()
    }
    exitProcess(0)
  }
  if (AutoLoginConfig.data.accounts.size > 1) {
    logger.warn { "Bikkuri v2.0 开始不再支持多帐号登录, 请不要添加多个 QQ 帐号。" }
    exitProcess(0)
  }

  AutoLoginConfig.data.accounts.forEach {
    logger.info { "Logging in ${it.account}" }
    val pwd by lazyUnsafe {
      it.password ?: error("Failed to login account ${it.account} with ${it.auth} authorization, password is empty")
    }
    val auth = when (it.auth) {
      Password -> BotAuthorization.byPassword(pwd)

      PasswordMd5 -> {
        val bytes = runCatching { HexFormat.of().parseHex(pwd) }.getOrNull()
          ?: error("Failed to login account ${it.account} with ${it.auth} authorization, hex is invalid")

        BotAuthorization.byPassword(bytes)
      }

      Qr -> BotAuthorization.byQRCode()
    }
    val bot = BotFactory.newBot(it.account, auth) {
      if (General.data.contactCache) {
        enableContactCache()
      } else {
        disableContactCache()
      }
      loginCacheEnabled = General.data.loginSecretCache

      File(globalWorkDirectory, "config").mkdir()
      this.fileBasedDeviceInfo("config/device.json")
      logger.info { "当前登录协议 ${it.protocol}, 版本: ${MiraiProtocolInternal.protocols[it.protocol]?.ver}" }
      protocol = it.protocol
    }

    bot.login()
  }
}

fun versionFormatted() = "v$VERSION-$BUILD_BRANCH-$COMMIT_HASH"

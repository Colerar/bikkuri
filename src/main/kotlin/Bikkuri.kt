package me.hbj.bikkuri

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.hbj.bikkuri.cmds.RegisteredCmd
import me.hbj.bikkuri.config.MAIN_GROUP
import me.hbj.bikkuri.config.NAME
import me.hbj.bikkuri.config.VERSION
import me.hbj.bikkuri.data.BackupTasks
import me.hbj.bikkuri.data.General
import me.hbj.bikkuri.data.Keygen
import me.hbj.bikkuri.data.ListenerData
import me.hbj.bikkuri.db.Blocklist
import me.hbj.bikkuri.db.BlocklistLink
import me.hbj.bikkuri.db.BotAccepted
import me.hbj.bikkuri.db.ForwardTable
import me.hbj.bikkuri.db.ForwardToGroupSet
import me.hbj.bikkuri.db.ForwardeeSet
import me.hbj.bikkuri.db.GuardLastUpdate
import me.hbj.bikkuri.db.GuardList
import me.hbj.bikkuri.db.JoinTimes
import me.hbj.bikkuri.events.onBotOffline
import me.hbj.bikkuri.events.onBotOnline
import me.hbj.bikkuri.events.onMemberJoin
import me.hbj.bikkuri.events.onMemberRequest
import me.hbj.bikkuri.events.onMessagePreSend
import me.hbj.bikkuri.events.onMessageReceived
import me.hbj.bikkuri.tasks.launchAutoApproveTask
import me.hbj.bikkuri.tasks.launchAutoKickTask
import me.hbj.bikkuri.tasks.launchBackupJob
import me.hbj.bikkuri.tasks.launchUpdateForwardTask
import me.hbj.bikkuri.tasks.launchUpdateGuardListTask
import me.hbj.bikkuri.tasks.setMessageTask
import net.mamoe.mirai.console.command.Command
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.utils.LoggerAdapters
import net.mamoe.mirai.utils.info
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection
import kotlin.io.path.absolutePathString

object Bikkuri : KotlinPlugin(
  JvmPluginDescription(id = MAIN_GROUP, name = NAME, version = VERSION) {
    author("Colerar")
  }
) {
  internal val registeredCmds by lazy {
    RegisteredCmd::class.sealedSubclasses
      .mapNotNull { it.objectInstance }
      .filterIsInstance(Command::class.java)
  }

  override fun onEnable() {
    LoggerAdapters.useLog4j2()
    logger.info { "Bikkuri Plugin Enabled, v$VERSION" }
    initYabapi()
    loadData()
    loadDb()
    registerCommands()
    subscribeEvents()
    launchTasks()
  }

  override fun onDisable() {
    CommandManager.unregisterAllCommands(this)
    cleanupData()
  }

  private fun loadData() =
    listOf(General, BackupTasks, ListenerData, Keygen).forEach { it.reload() }

  private fun loadDb() {
    val path = resolveDataPath("data.db").absolutePathString()
    val config = HikariConfig().apply {
      jdbcUrl = "jdbc:sqlite:$path"
      driverClassName = "org.sqlite.JDBC"
      maximumPoolSize = 1
    }
    val source = HikariDataSource(config)
    Database.connect(source)
    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
    transaction {
      SchemaUtils.create(
        Blocklist,
        BlocklistLink,
        BotAccepted,
        JoinTimes,
        GuardList,
        GuardLastUpdate,
        ForwardTable,
        ForwardToGroupSet,
        ForwardeeSet,
      )
    }
  }

  private fun cleanupData() = runBlocking {
    Keygen.cleanup()
    GuardList.cleanup()
  }

  private fun subscribeEvents() = GlobalEventChannel.apply {
    onBotOnline()
    onBotOffline()
    onMessageReceived()
    onMessagePreSend()
    onMemberJoin()
    onMemberRequest()
  }

  private fun registerCommands() {
    registeredCmds.forEach(CommandManager::registerCommand)
    logger.info("Registered ${registeredCmds.count()} commands by Bikkuri.")
  }

  private fun launchTasks() = launch {
    listOf(
      ::launchAutoKickTask,
      ::launchAutoApproveTask,
      ::launchUpdateGuardListTask,
      ::setMessageTask,
      ::launchBackupJob,
      ::launchUpdateForwardTask,
    ).forEach {
      coroutineScope { it() }
    }
  }
}

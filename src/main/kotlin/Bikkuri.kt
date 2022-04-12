package me.hbj.bikkuri

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.hbj.bikkuri.cmds.Approve
import me.hbj.bikkuri.cmds.Backup
import me.hbj.bikkuri.cmds.Block
import me.hbj.bikkuri.cmds.CheckLogin
import me.hbj.bikkuri.cmds.Config
import me.hbj.bikkuri.cmds.LoginBili
import me.hbj.bikkuri.cmds.Sign
import me.hbj.bikkuri.cmds.Status
import me.hbj.bikkuri.cmds.Version
import me.hbj.bikkuri.config.MAIN_GROUP
import me.hbj.bikkuri.config.NAME
import me.hbj.bikkuri.config.VERSION
import me.hbj.bikkuri.data.General
import me.hbj.bikkuri.data.Keygen
import me.hbj.bikkuri.data.ListenerData
import me.hbj.bikkuri.db.Blocklist
import me.hbj.bikkuri.db.BlocklistLink
import me.hbj.bikkuri.db.BotAccepted
import me.hbj.bikkuri.db.GuardLastUpdate
import me.hbj.bikkuri.db.GuardList
import me.hbj.bikkuri.events.onBotOffline
import me.hbj.bikkuri.events.onBotOnline
import me.hbj.bikkuri.events.onMemberJoin
import me.hbj.bikkuri.events.onMemberRequest
import me.hbj.bikkuri.events.onMessagePreSend
import me.hbj.bikkuri.events.onMessageReceived
import me.hbj.bikkuri.tasks.launchAutoApproveTask
import me.hbj.bikkuri.tasks.launchAutoKickTask
import me.hbj.bikkuri.tasks.launchUpdateGuardListTask
import me.hbj.bikkuri.tasks.setMessageTask
import net.mamoe.mirai.console.command.Command
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.utils.LoggerAdapters
import net.mamoe.mirai.utils.MiraiExperimentalApi
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
    listOf<Command>(Approve, Backup, Block, CheckLogin, Config, LoginBili, Sign, Status, Version)
  }

  @OptIn(MiraiExperimentalApi::class)
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
    listOf(General, ListenerData, Keygen).forEach { it.reload() }

  private fun loadDb() {
    val path = resolveDataPath("data.db").absolutePathString()
    val db = Database.connect("jdbc:sqlite:$path", "org.sqlite.JDBC")
    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
    transaction {
      SchemaUtils.create(Blocklist, BlocklistLink, BotAccepted, GuardList, GuardLastUpdate)
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
  }

  private fun launchTasks() = launch {
    listOf(
      ::launchAutoKickTask,
      ::launchAutoApproveTask,
      ::launchUpdateGuardListTask,
      ::setMessageTask,
    ).forEach {
      coroutineScope { it() }
    }
  }
}

package me.hbj.bikkuri

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.hbj.bikkuri.cmds.Config
import me.hbj.bikkuri.cmds.LoginBili
import me.hbj.bikkuri.cmds.Sign
import me.hbj.bikkuri.cmds.Status
import me.hbj.bikkuri.cmds.Version
import me.hbj.bikkuri.config.MAIN_GROUP
import me.hbj.bikkuri.config.NAME
import me.hbj.bikkuri.config.VERSION
import me.hbj.bikkuri.data.AutoApprove
import me.hbj.bikkuri.data.Keygen
import me.hbj.bikkuri.data.LastMsg
import me.hbj.bikkuri.data.ListenerData
import me.hbj.bikkuri.data.LiverGuard
import me.hbj.bikkuri.events.onBotOffline
import me.hbj.bikkuri.events.onBotOnline
import me.hbj.bikkuri.events.onMemberRequest
import me.hbj.bikkuri.events.onNewMember
import me.hbj.bikkuri.events.onReceivedMessage
import me.hbj.bikkuri.tasks.launchAutoApproveTask
import me.hbj.bikkuri.tasks.launchAutoKickTask
import me.hbj.bikkuri.tasks.launchUpdateGuardListTask
import net.mamoe.mirai.console.command.Command
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.utils.LoggerAdapters
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.info

object Bikkuri : KotlinPlugin(
  JvmPluginDescription(id = MAIN_GROUP, name = NAME, version = VERSION) {
    author("Colerar")
  }
) {
  internal val registeredCmds by lazy {
    listOf<Command>(Status, Version, LoginBili, Config, Sign)
  }

  @OptIn(MiraiExperimentalApi::class)
  override fun onEnable() {
    LoggerAdapters.useLog4j2()
    logger.info { "Bikkuri Plugin Enabled, v$VERSION" }
    initYabapi()
    loadData()
    registerCommands()
    subscribeEvents()
    launchTasks()
  }

  override fun onDisable() {
    CommandManager.unregisterAllCommands(this)
    cleanupData()
  }

  private fun loadData() =
    listOf(ListenerData, Keygen, AutoApprove, LastMsg, LiverGuard).forEach { it.reload() }

  private fun cleanupData() = runBlocking {
    Keygen.cleanup()
    LiverGuard.cleanup()
  }

  private fun subscribeEvents() = GlobalEventChannel.apply {
    onBotOnline()
    onBotOffline()
    onReceivedMessage()
    onNewMember()
    onMemberRequest()
  }

  private fun registerCommands() {
    registeredCmds.forEach(CommandManager::registerCommand)
  }

  private fun launchTasks() = launch {
    launchAutoKickTask()
    launchAutoApproveTask()
    coroutineScope {
      launchUpdateGuardListTask().invokeOnCompletion { me.hbj.bikkuri.tasks.jobMap.clear() }
    }
  }
}

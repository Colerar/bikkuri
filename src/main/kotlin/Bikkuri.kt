package me.hbj.bikkuri

import me.hbj.bikkuri.cmds.Config
import me.hbj.bikkuri.cmds.LoginBili
import me.hbj.bikkuri.cmds.Sign
import me.hbj.bikkuri.cmds.Version
import me.hbj.bikkuri.config.VERSION
import me.hbj.bikkuri.data.AutoApprove
import me.hbj.bikkuri.data.Keygen
import me.hbj.bikkuri.data.LastMsg
import me.hbj.bikkuri.data.ListenerData
import me.hbj.bikkuri.events.onBotOnline
import me.hbj.bikkuri.events.onMemberRequest
import me.hbj.bikkuri.events.onNewMember
import me.hbj.bikkuri.events.onReceivedMessage
import me.hbj.bikkuri.tasks.launchAutoKickTask
import net.mamoe.mirai.console.command.Command
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.info

object Bikkuri : KotlinPlugin(
  JvmPluginDescription(
    id = "me.hbj.bikkuri",
    name = "Bikkuri",
    version = VERSION,
  ) {
    author("Colerar")
  }
) {
  internal val registeredCmds = listOf<Command>(Version, LoginBili, Config, Sign, Version)

  @OptIn(MiraiExperimentalApi::class)
  override fun onEnable() {
    logger.info { "Bikkuri Plugin Enabled, v$VERSION" }
    initYabapi()
    loadData()
    registerCommands()
    subscribeEvents()
    launchTasks()
  }

  override fun onDisable() {
    CommandManager.unregisterAllCommands(this)
  }

  private fun loadData() =
    listOf(ListenerData, Keygen, AutoApprove, LastMsg).forEach { it.reload() }

  private fun subscribeEvents() = GlobalEventChannel.apply {
    onBotOnline()
    onReceivedMessage()
    onNewMember()
    onMemberRequest()
  }

  private fun registerCommands() {
    registeredCmds.forEach(CommandManager::registerCommand)
    Keygen.cleanup()
  }

  private fun launchTasks() {
    launchAutoKickTask()
  }
}

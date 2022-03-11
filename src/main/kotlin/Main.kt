package me.hbj.bikkuri

import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.enable
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.load
import net.mamoe.mirai.console.terminal.MiraiConsoleTerminalLoader
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import java.io.File

private fun setupWorkingDir() {
  // see: net.mamoe.mirai.console.terminal.MiraiConsoleImplementationTerminal
  System.setProperty("user.dir", File("mirai").absolutePath)
}

@OptIn(ConsoleExperimentalApi::class)
fun main() = runBlocking {
  setupWorkingDir()

  MiraiConsoleTerminalLoader.startAsDaemon()

  Bikkuri.apply {
    load()
    enable()
  }

  MiraiConsole.job.join()
}

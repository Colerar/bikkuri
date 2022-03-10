package me.hbj

import me.hbj.bikkuri.Bikkuri
import me.hbj.bikkuri.config.TEST_ID
import me.hbj.bikkuri.config.TEST_PWD
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.enable
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.load
import net.mamoe.mirai.console.terminal.MiraiConsoleTerminalLoader
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import java.io.File

fun setupWorkingDir() {
  // see: net.mamoe.mirai.console.terminal.MiraiConsoleImplementationTerminal
  System.setProperty("user.dir", File("debug-sandbox").absolutePath)
}

@OptIn(ConsoleExperimentalApi::class)
suspend fun main() {
  setupWorkingDir()

  MiraiConsoleTerminalLoader.startAsDaemon()

  Bikkuri.apply {
    load()
    enable()
  }

  /**
   * Should define test properties in `local.properties`
   * TEST_ID - bikkuri.test.id
   * TEST_PWD - bikkuri.test.pwd
   */
  MiraiConsole.addBot(
    TEST_ID ?: error("'bikkuri.test.id'[Long] should be defined in 'local.properties' for test"),
    TEST_PWD ?: error("'bikkuri.test.pwd'[String] should be defined in 'local.properties' for test"),
  ).alsoLogin()

  MiraiConsole.job.join()
}

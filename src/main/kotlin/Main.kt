package me.hbj.bikkuri

import kotlinx.coroutines.runBlocking
import me.hbj.bikkuri.util.getJarLocation
import net.mamoe.mirai.console.ConsoleFrontEndImplementation
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.MiraiConsoleImplementation.Companion.start
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.enable
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.load
import net.mamoe.mirai.console.terminal.MiraiConsoleImplementationTerminal
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import java.io.File

private fun setupWorkingDir(workDir: File?) {
  // see: net.mamoe.mirai.console.terminal.MiraiConsoleImplementationTerminal
  val workDir1 = workDir ?: System.getProperty("bikkuri_workdir")?.let { File(it) }
  System.setProperty("user.dir", File(workDir1 ?: getJarLocation(), "mirai").absolutePath)
}

@Suppress("NOTHING_TO_INLINE")
@OptIn(ConsoleExperimentalApi::class, ConsoleFrontEndImplementation::class)
suspend fun setupTerminal(workDir: File? = null) {
  setupWorkingDir(workDir)

  MiraiConsoleImplementationTerminal().start()
  Class.forName("net.mamoe.mirai.console.terminal.ConsoleThreadKt").apply {
    getDeclaredMethod("startupConsoleThread").invoke(this)
  }

  Bikkuri.apply {
    load()
    enable()
  }

  MiraiConsole.job.join()
}

fun main() = runBlocking {
  setupTerminal()
}

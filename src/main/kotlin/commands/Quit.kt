package me.hbj.bikkuri.commands

import me.hbj.bikkuri.command.Command
import kotlin.system.exitProcess

class Quit : Command(Quit) {
  override suspend fun run() {
    exitProcess(0)
  }

  companion object : Entry(
    name = "quit",
    help = "退出 Bikkuri",
    alias = listOf("exit", "stop"),
  )
}

package me.hbj.bikkuri.exception.command

import net.mamoe.mirai.console.command.Command

open class CommandCancellation(
  val command: Command?,
  override val cause: Throwable? = null,
  override val message: String? = "Command ${command?.primaryName} Cancelled"
) : Exception()

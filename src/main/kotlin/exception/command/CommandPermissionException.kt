package me.hbj.bikkuri.exception.command

import net.mamoe.mirai.console.command.Command
import net.mamoe.mirai.console.command.CommandSender

class CommandPermissionException(
  sender: CommandSender,
  command: Command,
  override val cause: Throwable? = null,
) : CommandCancellation(command) {
  override val message: String = "${sender.user?.id} has no permission to execute ${command.primaryName}"
}

fun Command.CommandPermissionException(sender: CommandSender): CommandPermissionException =
  CommandPermissionException(sender, this)

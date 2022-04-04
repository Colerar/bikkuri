package me.hbj.bikkuri.util

import kotlinx.coroutines.CancellationException
import me.hbj.bikkuri.exception.command.CommandCancellation
import me.hbj.bikkuri.exception.command.CommandPermissionException
import mu.KotlinLogging
import net.mamoe.mirai.console.command.Command
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.FriendCommandSender
import net.mamoe.mirai.console.command.MemberCommandSender
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.buildMessageChain
import java.util.Vector

private val logger = KotlinLogging.logger { }

private val vector = Vector<User>()

suspend fun <T : User?> T.cmdLock(action: suspend T.() -> Unit) {
  if (vector.contains(this)) return
  vector.add(this)
  action()
  vector.remove(this)
}

suspend inline fun <T : CommandSender> T.cmdLock(crossinline action: suspend T.() -> Unit) {
  this.user.cmdLock { action() }
}

fun Command.requireOperator(sender: MemberCommandSender) {
  if (!sender.user.isOperator()) throw CommandPermissionException(sender, this)
}

suspend fun Command.require(sender: MemberCommandSender, require: Boolean, lazyMessage: () -> String) {
  if (!require) {
    sender.group.sendMessage(lazyMessage())
    throw CommandCancellation(this, IllegalStateException(lazyMessage()))
  }
}

suspend fun Command.require(sender: FriendCommandSender, require: Boolean, lazyMessage: () -> String) {
  if (!require) {
    sender.sendMessage(lazyMessage())
    throw CommandCancellation(this, IllegalStateException(lazyMessage()))
  }
}

fun Command.require(require: Boolean, lazyMessage: () -> String) {
  if (!require) throw CommandCancellation(this, IllegalStateException(lazyMessage()))
}

fun Command.require(require: Boolean) {
  if (!require) throw CommandCancellation(this)
}

@OptIn(ExperimentalCommandDescriptors::class, ConsoleExperimentalApi::class)
suspend fun CommandSender.executeCommandSafely(message: MessageChain) {
  cmdLock {
    try {
      CommandManager.executeCommand(this, message, false)
    } catch (e: CommandCancellation) {
      logger.trace(e) { "Cancelled Command ${e.command.primaryName}:" }
    } catch (e: CancellationException) {
      logger.warn(e) { "Cancelled command:" }
    }
  }
}

@OptIn(ExperimentalCommandDescriptors::class, ConsoleExperimentalApi::class)
suspend fun CommandSender.executeCommandSafely(message: String) {
  executeCommandSafely(buildMessageChain { add(message) })
}

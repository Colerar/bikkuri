package me.hbj.bikkuri.command

import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import me.hbj.bikkuri.utils.ModuleScope
import me.hbj.bikkuri.utils.suggestTypo
import moe.sdl.yac.core.CommandResult
import moe.sdl.yac.core.CommandResult.Error
import moe.sdl.yac.core.PrintHelpMessage
import moe.sdl.yac.core.parseToArgs
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.contact.isOperator
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

private val logger = mu.KotlinLogging.logger {}

abstract class AbstractCommandNode<TSender : CommandSender>(
  val entry: Command.Entry,
  val creator: (sender: TSender) -> Command,
)

class CommandNode(
  entry: Command.Entry,
  creator: (CommandSender) -> Command,
) : AbstractCommandNode<CommandSender>(entry, creator)

class ConsoleCommandNode(
  entry: Command.Entry,
  creator: (ConsoleCommandSender) -> Command,
) : AbstractCommandNode<ConsoleCommandSender>(entry, creator)

class MiraiCommandNode(
  entry: Command.Entry,
  creator: (MiraiCommandSender) -> Command,
) : AbstractCommandNode<MiraiCommandSender>(entry, creator)

object CommandManager {
  val commandMap: Map<String, AbstractCommandNode<*>>
    get() = cmdMap

  private val cmdMap: MutableMap<String, AbstractCommandNode<*>> = ConcurrentHashMap()

  // A map to save the registered commands with alias.
  private val aliasMap: MutableMap<String, AbstractCommandNode<*>> = ConcurrentHashMap()

  val commandEntries: List<Command.Entry> get() = cmdMap.entries.map { it.value.entry }

  private var commandScope = ModuleScope("CommandManager")

  internal fun init(parentContext: CoroutineContext = EmptyCoroutineContext) {
    commandScope = ModuleScope("CommandManager", parentContext)
  }

  @Suppress("unused")
  fun registerCommand(entry: Command.Entry, creator: (CommandSender) -> Command) {
    registerCommand(CommandNode(entry, creator))
  }

  @Suppress("MemberVisibilityCanBePrivate")
  fun registerCommand(commandNode: AbstractCommandNode<*>) {
    val name = commandNode.entry.name
    val aliases = commandNode.entry.alias

    cmdMap.putIfAbsent(name, commandNode)?.also {
      logger.warn { "Command name '$name' conflict." }
    }

    aliases.forEach { alias ->
      aliasMap.putIfAbsent(alias, commandNode)?.also {
        logger.warn { "Alias name '$aliases' conflict." }
      }
    }
  }

  fun registerCommands(collection: Collection<AbstractCommandNode<*>>): Unit =
    collection.forEach { registerCommand(it) }

  fun invokeCommand(
    sender: CommandSender,
    rawMsg: String,
  ): Job = commandScope.launch {
    if (rawMsg.isBlank()) {
      if (sender is MiraiCommandSender) return@launch
      sender.sendMessage("No command input")
      return@launch
    }

    val args = rawMsg.parseToArgs()
    val mainCommand = args[0]

    val cmd =
      cmdMap[mainCommand] ?: aliasMap[mainCommand] ?: run {
        if (sender is MiraiCommandSender) return@launch
        val mainTypo = suggestTypo(mainCommand, cmdMap.keys.toList())
        mainTypo?.let {
          sender.sendMessage("No such command \"$mainCommand\". Did you mean \"$mainTypo\"?")
        } ?: sender.sendMessage("No such command \"$mainCommand\"")
        return@launch
      }

    val result: CommandResult = run {
      when (cmd) {
        is CommandNode -> cmd.creator(sender).execute(args)

        is ConsoleCommandNode -> if (sender is ConsoleCommandSender) {
          cmd.creator(sender).execute(args)
        } else {
          Error(null, userMessage = "You do not have the permission to execute this command.")
        }

        is MiraiCommandNode -> if (sender is MiraiCommandSender) {
          cmd.creator(sender).execute(args)
        } else {
          Error(null, userMessage = "You do not have the permission to execute this command.")
        }

        else -> Error(null, userMessage = "Internal error")
      }
    }

    if (result !is Error) return@launch
    val cause = result.cause
    if (sender is MiraiCommandSender && !sender.contact.canSendErrorMsg()) {
      return@launch
    }
    val msg = buildString {
      append(result.userMessage)
      if (cause is PrintHelpMessage && cmd.entry.alias.isNotEmpty()) {
        append("\n\n别名: ${cmd.entry.alias.joinToString()}")
      }
    }
    sender.sendMessage(msg)
  }

  /**
   * @param mainCommand main command or alias
   * @return [Boolean] has or not
   */
  fun hasCommand(mainCommand: String): Boolean =
    cmdMap.containsKey(mainCommand) || aliasMap.containsKey(mainCommand)
}

private suspend fun Command.execute(args: List<String>) = this.main(args.drop(1))

fun Contact.canSendErrorMsg() =
  (this is Friend) || (this is NormalMember && this.isOperator())

package me.hbj.bikkuri.command

import me.hbj.bikkuri.utils.lazyUnsafe
import moe.sdl.yac.core.CliktCommand
import moe.sdl.yac.core.context
import net.mamoe.mirai.utils.ConcurrentHashMap
import java.lang.ref.SoftReference

private typealias Aliases = Map<String, List<String>>

private val aliasesCaches = ConcurrentHashMap<String, SoftReference<Aliases>>()

abstract class Command(
  val entry: Entry,
  option: Option = Option(),
) : CliktCommand(
  name = entry.name,
  help = entry.help,
  invokeWithoutSubcommand = option.invokeWithoutSubCommand,
  printHelpOnEmptyArgs = option.printHelpOnEmptyArgs,
  allowMultipleSubcommands = option.allowMultipleSubcommands,
  treatUnknownOptionsAsArgs = option.treatUnknownOptionsAsArgs,
) {
  init {
    context {
      localization = CommandL10nChinese
    }
  }

  private fun computeAliases(subcommands: List<CliktCommand>): Aliases {
    val map = HashMap<String, List<String>>(subcommands.size)
    subcommands
      .asSequence()
      .filterIsInstance<Command>()
      .filter { it.entry.alias.isNotEmpty() }
      .forEach { command ->
        val entry = command.entry
        entry.alias.forEach { map[it] = listOf(entry.name) }
      }
    return map
  }

  private val aliases: Aliases by lazyUnsafe {
    val subcommands = registeredSubcommands()
    if (subcommands.isEmpty()) return@lazyUnsafe emptyMap()
    val cached = aliasesCaches.computeIfAbsent(entry.name) {
      SoftReference(computeAliases(subcommands))
    }.get()

    if (cached == null) {
      val ref = computeAliases(subcommands)
      aliasesCaches[entry.name] = SoftReference(ref)
      return@lazyUnsafe ref
    }
    cached
  }

  override fun aliases() = aliases

  data class Option(
    val invokeWithoutSubCommand: Boolean = false,
    val printHelpOnEmptyArgs: Boolean = false,
    val allowMultipleSubcommands: Boolean = false,
    val treatUnknownOptionsAsArgs: Boolean = false,
  )

  /**
   * @param help, will be invoked with i18n
   */
  open class Entry(
    val name: String,
    val help: String = "",
    val alias: List<String> = emptyList(),
  )
}

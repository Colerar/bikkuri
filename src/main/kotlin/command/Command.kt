package me.hbj.bikkuri.command

import moe.sdl.yac.core.CliktCommand
import moe.sdl.yac.core.context

abstract class Command(
  entry: Entry,
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

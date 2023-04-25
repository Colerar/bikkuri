package me.hbj.bikkuri.commands

import me.hbj.bikkuri.command.*
import moe.sdl.yac.core.CliktError
import moe.sdl.yac.core.UsageError
import moe.sdl.yac.parameters.arguments.argument
import moe.sdl.yac.parameters.arguments.default
import moe.sdl.yac.parameters.options.convert
import moe.sdl.yac.parameters.options.default
import moe.sdl.yac.parameters.options.option
import moe.sdl.yac.parameters.types.int
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.contact.isOperator

private const val MAX_PAGE_NUM = 20

private const val MIN_PAGE_NUM = 1

private const val DEFAULT_PAGE_SIZE = 10

class Help(
  private val sender: CommandSender,
) : Command(Help) {
  init {
    if (sender is MiraiCommandSender) {
      if (sender.contact !is Friend && sender.contact !is NormalMember) {
        throw CliktError("Permission Denied")
      }
      if (sender.contact is NormalMember && !sender.contact.isOperator()) {
        throw CliktError("Permission Denied")
      }
    }
  }

  private val pageNum by argument(
    name = "页码",
  ).int().default(MIN_PAGE_NUM)
  private val pageSize by option(
    "--page-size",
    "-s",
    help = "每页大小",
  ).int().convert {
    it.coerceIn(MIN_PAGE_NUM..MAX_PAGE_NUM)
  }.default(DEFAULT_PAGE_SIZE)

  override suspend fun run() {
    val cmdList = CommandManager.commandEntries
    // Take out the page items from command list.
    val pageItems = cmdList.chunked(pageSize)
    // Throw exception when the page number exceed.
    if (pageNum !in MIN_PAGE_NUM..pageItems.size) {
      throw UsageError("The page number exceeds the total number of pages.")
    }

    // Build the message and send to the sender
    sender.sendMessage(
      buildString {
        appendLine("===== 第 $pageNum 页, 共 ${pageItems.size} 页 =====")
        val entries = pageItems[pageNum - 1]
        val maxLen = entries.map { it.name }.maxOf { it.length }
        entries.forEach {
          if (sender is ConsoleCommandSender) {
            append(it.name.padEnd(maxLen, ' '))
          } else {
            append(it.name)
          }
          append(" >> ")
          if (it.help.isNotBlank()) {
            append(it.help)
          } else {
            append("暂无简介")
          }
          appendLine()
        }
        append("用 \"<命令> --help\" 查询命令详细用法")
      },
    )
  }

  companion object : Entry(
    name = "help",
    help = "打印命令帮助",
    alias = listOf("?"),
  )
}

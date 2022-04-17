package me.hbj.bikkuri.cmds

import me.hbj.bikkuri.Bikkuri
import me.hbj.bikkuri.util.RuntimeUtil
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.SimpleCommand

object Status :
  SimpleCommand(
    Bikkuri,
    primaryName = "bstatus",
    secondaryNames = arrayOf("bikkuri"),
    description = "查看当前机器人系统状态"
  ),
  RegisteredCmd {
  private suspend fun shortInfo() = """
    OS   | ${RuntimeUtil.osName}
    TIME | ${RuntimeUtil.uptime}
    MEM  | ${RuntimeUtil.Mem.used.toShow()}/${RuntimeUtil.Mem.total.toShow()}
    CPU  | ${RuntimeUtil.getCpuRate()}
  """.trimIndent()

  private suspend fun longInfo() = """
    OS   | ${RuntimeUtil.osName}
    TIME | ${RuntimeUtil.uptime}
    CPU  | ${RuntimeUtil.getCpuRate()}
  """.trimIndent() + "\n" + RuntimeUtil.Mem.detailed

  @Handler
  suspend fun CommandSender.status(option: String? = null) {
    val info = when (option) {
      "-l" -> longInfo()
      "-long" -> longInfo()
      else -> shortInfo()
    }
    sendMessage(info)
  }
}

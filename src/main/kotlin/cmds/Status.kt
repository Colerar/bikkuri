package me.hbj.bikkuri.cmds

import me.hbj.bikkuri.Bikkuri
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import oshi.SystemInfo
import kotlin.time.DurationUnit
import kotlin.time.toDuration

object Status : SimpleCommand(Bikkuri, "bstatus", "bikkuri", description = "查看当前机器人系统状态") {
  private val sys = SystemInfo()
  private val os
    get() = sys.operatingSystem

  private val curProcess
    get() = os.processes.firstOrNull { it.processID == os.processId }

  @Handler
  suspend fun CommandSender.status() {
    sendMessage(
      """
      当前系统: $os
      运行时间: ${getUptime()} | MEM ${getMemoryRate().show()} | CPU ${getCpuRate().show()}
    """.trimIndent()
    )
  }

  private fun Double.show() = String.format("%.2f%%", this)

  private fun getUptime(): String {
    val runtime = curProcess?.upTime
      ?.toDuration(DurationUnit.MILLISECONDS) ?: return "unk"

    return runtime.toComponents { h, m, s, _ ->
      val hour = h.toString().padStart(2, '0')
      val min = m.toString().padStart(2, '0')
      val sec = s.toString().padStart(2, '0')
      "$hour:$min:$sec"
    }
  }

  private fun getMemoryRate(): Double = with(sys.hardware.memory) {
    return available.toDouble() / total.toDouble()
  }

  private fun getCpuRate(): Double = with(sys.hardware.processor) {
    return getSystemCpuLoadBetweenTicks(systemCpuLoadTicks)
  }
}

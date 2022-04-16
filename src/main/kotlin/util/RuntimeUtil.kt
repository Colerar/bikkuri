package me.hbj.bikkuri.util

import kotlinx.coroutines.delay
import oshi.SystemInfo
import java.lang.management.ManagementFactory
import kotlin.time.DurationUnit
import kotlin.time.toDuration

object RuntimeUtil {
  private val sys = SystemInfo()
  private val os
    get() = sys.operatingSystem

  private val curProcess
    get() = os.processes.firstOrNull { it.processID == os.processId }

  private val runtimeMXBean by lazy {
    ManagementFactory.getRuntimeMXBean()
  }

  val jvmVersion by lazy {
    with(runtimeMXBean) {
      "$vmName $vmVersion"
    }
  }

  val osName: String by lazy {
    os.toString()
  }

  object Mem {
    private val memMXBean = ManagementFactory.getMemoryMXBean()

    val total
      get() = Size(Runtime.getRuntime().totalMemory())

    val free
      get() = Size(Runtime.getRuntime().freeMemory())

    val max
      get() = Size(Runtime.getRuntime().maxMemory())

    val used
      get() = total - free

    private fun String.pad(size: Int) = padEnd(size, ' ')

    val detailed: String
      get() {
        val heap = memMXBean.heapMemoryUsage
        val nonHeap = memMXBean.nonHeapMemoryUsage
        val ch = Size(heap.committed).toShow()
        val ih = Size(heap.init).toShow()
        val uh = Size(heap.used).toShow()
        val mh = Size(heap.max).toShow()
        val cn = Size(nonHeap.committed).toShow()
        val iN = Size(nonHeap.init).toShow()
        val un = Size(nonHeap.used).toShow()
        val mn = Size(nonHeap.max).toShow()
        return """
          MEM  |   used    | committed |    init   |    max    |
          Heap | ${uh.pad(9)} | ${ch.pad(9)} | ${ih.pad(9)} | ${mh.pad(9)} |
          Non  | ${un.pad(9)} | ${cn.pad(9)} | ${iN.pad(9)} | ${mn.pad(9)} |
        """.trimIndent()
      }
  }

  val uptime: String
    get() {
      val runtime = curProcess?.upTime
        ?.toDuration(DurationUnit.MILLISECONDS) ?: return "unk"
      return runtime.toHMS()
    }

  suspend fun getCpuRate(): String {
    val lastProcess = curProcess
    delay(1_000)
    return curProcess?.getProcessCpuLoadBetweenTicks(lastProcess)?.toPercent() ?: "unk"
  }
}

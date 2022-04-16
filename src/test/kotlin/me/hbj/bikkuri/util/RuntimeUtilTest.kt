package me.hbj.bikkuri.util

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class RuntimeUtilTest {
  @Test
  fun osName(): Unit = with(RuntimeUtil) {
    osName.alsoPrint()
  }

  @Test
  fun jvmVersion(): Unit = with(RuntimeUtil) {
    jvmVersion.alsoPrint()
  }

  @Test
  fun mem(): Unit = with(RuntimeUtil.Mem) {
    println(
      """
      Used  : ${used.toShow()}
      Free  : ${free.toShow()}
      Total : ${total.toShow()}
      Max   : ${max.toShow()}
    """.trimIndent()
    )
  }

  @Test
  fun uptime(): Unit = with(RuntimeUtil) {
    uptime.alsoPrint()
  }

  @Test
  fun cpuLoad(): Unit = with(RuntimeUtil) {
    runBlocking {
      getCpuRate().alsoPrint()
    }
  }
}

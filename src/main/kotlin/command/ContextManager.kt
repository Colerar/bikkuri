package me.hbj.bikkuri.command

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Job
import net.mamoe.mirai.utils.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

class ContextManager {
  val ctxMap = ConcurrentHashMap<JobIdentity, Job>()

  fun contextBegin(i: JobIdentity, jobInitializer: () -> Job): Boolean {
    if (ctxMap.containsKey(i)) return false
    val job = jobInitializer()
    job.invokeOnCompletion {
      ctxMap.remove(i)
    }
    ctxMap[i] = job
    return true
  }

  fun cancelJob(i: JobIdentity) {
    ctxMap[i]?.apply {
      logger.info { "Cancel command job for $i" }
      this.cancel()
    }
  }
}

data class JobIdentity(
  val bot: Long,
  val group: Long?,
  val qq: Long,
)

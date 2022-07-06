package me.hbj.bikkuri.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import mu.KLogger
import mu.KotlinLogging
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

private val logger = KotlinLogging.logger {}

/**
 * Provide a common [CoroutineScope] for module
 * with common [CoroutineExceptionHandler] and [Dispatchers.Default]
 *
 * In general, you should add a [ModuleScope] as a class member field
 * or object member field with some `init(parentCoroutineContext)` method.
 * And launch or dispatch jobs coroutines by [ModuleScope]
 *
 * @property parentJob specified [Job] with parent coroutine context [Job]
 *
 * @param moduleName coroutine name, and name also would appear
 * @param parentContext parent scope [CoroutineContext]
 * @param dispatcher custom [CoroutineDispatcher]
 * @param exceptionHandler custom expcetion handler lambda
 * with [CoroutineContext], [Throwable], [KLogger] the specified logger, [String] module name
 */
open class ModuleScope(
  private val moduleName: String = "UnnamedModule",
  parentContext: CoroutineContext = EmptyCoroutineContext,
  dispatcher: CoroutineDispatcher = Dispatchers.Default,
  exceptionHandler: (CoroutineContext, Throwable, KLogger, String) -> Unit =
    { _, e, _, _ -> logger.error(e) { "Caught Exception on $moduleName" } }
) : CoroutineScope {

  val parentJob = SupervisorJob(parentContext[Job])

  override val coroutineContext: CoroutineContext =
    parentContext + parentJob + CoroutineName(moduleName) + dispatcher +
      CoroutineExceptionHandler { context, e ->
        exceptionHandler(context, e, logger, moduleName)
      }

  fun dispose() {
    parentJob.cancel()
    onClosed()
  }

  open fun onClosed() {
  }
}

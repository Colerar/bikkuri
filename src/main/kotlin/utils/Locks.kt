package me.hbj.bikkuri.utils

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext

/**
 * Use reenterable mutex lock in coroutine with different threads
 *
 * It will store mutex lock in [CoroutineContext] with [ReentrantMutexContextElement]
 */
@OptIn(ExperimentalContracts::class)
suspend fun <T> Mutex.withReentrantLock(block: suspend () -> T): T {
  contract {
    callsInPlace(block, InvocationKind.EXACTLY_ONCE)
  }
  val key = ReentrantMutexContextKey(this)
  // call block directly when this mutex is already locked in the context
  if (currentCoroutineContext()[key] != null) return block()
  // otherwise add it to the context and lock the mutex
  return withContext(ReentrantMutexContextElement(key)) {
    withLock { block() }
  }
}

class ReentrantMutexContextElement(
  override val key: ReentrantMutexContextKey,
) : CoroutineContext.Element

data class ReentrantMutexContextKey(
  val mutex: Mutex,
) : CoroutineContext.Key<ReentrantMutexContextElement>

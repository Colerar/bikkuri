@file:Suppress("NOTHING_TO_INLINE")

package me.hbj.bikkuri.utils

inline fun <T> lazyPublication(noinline initializer: () -> T) =
  lazy(LazyThreadSafetyMode.PUBLICATION, initializer)

inline fun <T> lazyUnsafe(noinline initializer: () -> T) =
  lazy(LazyThreadSafetyMode.NONE, initializer)

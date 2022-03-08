package me.hbj.bikkuri.util

@Suppress("NOTHING_TO_INLINE")
inline fun String.clearIndent() = this.trimIndent().removeSuffix("\n")

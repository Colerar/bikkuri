package me.hbj.bikkuri.util

@Suppress("NOTHING_TO_INLINE")
inline fun String.clearIndent() = this.trimIndent().removeSuffix("\n")

internal val uidRegex = Regex("""^\s*(UID:?)?([0-9]+)\s*$""")

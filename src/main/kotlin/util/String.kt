package me.hbj.bikkuri.util

import me.hbj.bikkuri.data.General

@Suppress("NOTHING_TO_INLINE")
inline fun String.clearIndent() = this.trimIndent().removeSuffix("\n")

internal val uidRegex = Regex("""^\s*(UID:)?([0-9]+)\s*$""")

internal val keygenRegex = Regex("""(\[?[${General.keygen.pattern}]+]?)""")

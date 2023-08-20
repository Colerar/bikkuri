package me.hbj.bikkuri.bili.util.string

import io.ktor.http.*

internal fun ContentType.toHeaderValue() = "$contentType/$contentSubtype"

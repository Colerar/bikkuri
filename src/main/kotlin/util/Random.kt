package me.hbj.bikkuri.util

import me.hbj.bikkuri.data.General

private val pattern by lazy {
  General.keygen.pattern.toList().toTypedArray()
}

fun randomKeygen(length: Int): String =
  StringBuilder(length).apply {
    repeat(length) {
      append(pattern.random())
    }
  }.toString()

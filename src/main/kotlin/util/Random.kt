package me.hbj.bikkuri.util

private val pattern by lazy {
  "1234567890QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm".toList().toTypedArray()
}

fun randomKeygen(length: Int): String =
  StringBuilder(length).apply {
    repeat(length) {
      append(pattern.random())
    }
  }.toString()

package me.hbj.bikkuri.util

import java.security.SecureRandom

private val rand by lazy { SecureRandom.getInstanceStrong() }

private val pattern by lazy {
  "1234567890QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm".toList()
}

fun randomKeygen(length: Int): String =
  StringBuilder(length).apply {
    repeat(length) {
      val idx = rand.nextInt(0, pattern.lastIndex)
      append(pattern[idx])
    }
  }.toString()

package me.hbj.bikkuri.util

private const val PATTERN = "0123456789"

fun randomKeygen(length: Int): String =
  StringBuilder(length).apply {
    repeat(length) {
      append(PATTERN.random())
    }
  }.toString()

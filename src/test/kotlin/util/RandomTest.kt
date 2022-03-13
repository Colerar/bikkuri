package me.hbj.bikkuri.util

import org.junit.jupiter.api.Test

class RandomTest {
  @Test
  fun randomKeygenTest() {
    randomKeygen(20).also(::println)
  }
}

package me.hbj.util

import me.hbj.bikkuri.util.randomKeygen
import kotlin.test.Test

class RandomTest {
    @Test
    fun randomKeygenTest() {
        randomKeygen(20).also(::println)
    }
}

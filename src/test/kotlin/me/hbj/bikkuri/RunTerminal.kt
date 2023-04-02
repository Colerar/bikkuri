package me.hbj.bikkuri

import me.hbj.bikkuri.config.TEST_DIR
import java.io.File

suspend fun main() {
  fixProtoVersion()
  System.setProperty("mirai.no-desktop", "")
  setupTerminal(File(TEST_DIR))
}

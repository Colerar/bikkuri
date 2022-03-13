package me.hbj.bikkuri.data

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value

object General : AutoSavePluginConfig("_General") {
  val retryTimes by value(5)
}

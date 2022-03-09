package me.hbj.bikkuri.data

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value

object AutoApprove : AutoSavePluginData("AutoApproveList") {
  // QQ group id to data
  val map: MutableMap<Long, AutoApproveData> by value()
}

@Serializable
class AutoApproveData(
  val set: MutableSet<Long> = mutableSetOf(),
)

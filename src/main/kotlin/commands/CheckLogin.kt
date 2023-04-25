package me.hbj.bikkuri.commands

import kotlinx.coroutines.coroutineScope
import me.hbj.bikkuri.client
import me.hbj.bikkuri.command.Command
import me.hbj.bikkuri.command.MiraiCommandSender
import moe.sdl.yabapi.api.getBasicInfo
import moe.sdl.yabapi.data.GeneralCode

class CheckLogin(private val sender: MiraiCommandSender) : Command(CheckLogin) {
  val member = memberOperator(sender)

  override suspend fun run(): Unit = coroutineScope cmd@{
    val resp = client.getBasicInfo()
    if (resp.code == GeneralCode.SUCCESS && resp.data.isLogin) {
      sender.sendMessage("🔍 当前登录的帐号 ${resp.data.username}(${resp.data.mid})")
    } else {
      sender.sendMessage("❌ 当前未登录: ${resp.code} - ${resp.message}")
    }
  }

  companion object : Entry(
    name = "check-login",
    help = "检查当前登陆的 B 站帐号",
    alias = listOf("checklogin"),
  )
}

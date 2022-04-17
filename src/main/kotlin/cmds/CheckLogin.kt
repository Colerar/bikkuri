package me.hbj.bikkuri.cmds

import me.hbj.bikkuri.Bikkuri
import me.hbj.bikkuri.client
import me.hbj.bikkuri.util.requireOperator
import moe.sdl.yabapi.api.getBasicInfo
import moe.sdl.yabapi.data.GeneralCode
import net.mamoe.mirai.console.command.MemberCommandSender
import net.mamoe.mirai.console.command.SimpleCommand

object CheckLogin : SimpleCommand(Bikkuri, "checklogin", "cl"), RegisteredCmd {
  @Handler
  suspend fun MemberCommandSender.handler() {
    requireOperator(this)
    val resp = client.getBasicInfo()
    if (resp.code == GeneralCode.SUCCESS && resp.data.isLogin) {
      sendMessage("🔍 当前登录的帐号 ${resp.data.username}(${resp.data.mid})")
    } else {
      sendMessage("❌ 当前未登录: ${resp.code} - ${resp.message}")
    }
  }
}

package me.hbj.bikkuri.commands

import me.hbj.bikkuri.command.CommandNode
import me.hbj.bikkuri.command.ConsoleCommandNode
import me.hbj.bikkuri.command.MiraiCommandNode

val defaultsCommand = listOf(
  CommandNode(Version) { sender -> Version(sender) },
  CommandNode(Help) { sender -> Help(sender) },
  ConsoleCommandNode(Quit) { Quit() },
  ConsoleCommandNode(LoginBili) { LoginBili() },
  MiraiCommandNode(Approve) { sender -> Approve(sender) },
  MiraiCommandNode(Block) { sender -> Block(sender) },
  MiraiCommandNode(Cancel) { sender -> Cancel(sender) },
  MiraiCommandNode(CheckLogin) { sender -> CheckLogin(sender) },
  MiraiCommandNode(Config) { sender -> Config(sender) },
  MiraiCommandNode(Sign) { sender -> Sign(sender) },
)
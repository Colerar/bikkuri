package me.hbj.bikkuri.commands

import me.hbj.bikkuri.command.MiraiCommandSender
import me.hbj.bikkuri.configs.General
import moe.sdl.yac.core.CliktError
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.contact.isOperator

fun memberOperator(sender: MiraiCommandSender): NormalMember {
  val member = sender.contact
  if (member !is NormalMember) throw CliktError("Not a normal member")
  val adminGroups = General.data.adminGroups
  if (!member.isOperator() && !adminGroups.contains(member.group.id)) {
    throw CliktError("Permission Denied")
  }
  return member
}

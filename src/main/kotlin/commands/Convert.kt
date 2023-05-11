package me.hbj.bikkuri.commands

import moe.sdl.yac.core.PrintMessage

fun convertAt(input: String) =
  input.trimStart('@').toLongOrNull() ?: throw PrintMessage("输入了无效的 QQ 号码: $input")

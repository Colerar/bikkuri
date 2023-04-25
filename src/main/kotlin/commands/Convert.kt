package me.hbj.bikkuri.commands

import moe.sdl.yac.core.PrintMessage

fun convertAt(input: String) =
  input.trimStart('@').toLongOrNull() ?: throw PrintMessage("At 输入有误")

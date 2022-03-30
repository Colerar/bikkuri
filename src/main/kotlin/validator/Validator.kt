package me.hbj.bikkuri.validator

import net.mamoe.mirai.console.command.MemberCommandSender
import net.mamoe.mirai.event.events.GroupMessageEvent

interface MessageValidator {
  /**
   * will be invoked asyncable
   */
  suspend fun beforeValidate(sender: MemberCommandSender)

  suspend fun validate(event: GroupMessageEvent): ValidatorOperation
}

enum class ValidatorOperation {
  PASSED,

  FAILED,

  ERROR,

  CONTINUED,
}

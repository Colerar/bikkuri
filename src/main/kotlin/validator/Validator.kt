package me.hbj.bikkuri.validator

import net.mamoe.mirai.console.command.MemberCommandSender
import net.mamoe.mirai.event.events.GroupMessageEvent
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

interface MessageValidator {
  /**
   * will be invoked asyncable
   */
  suspend fun beforeValidate(sender: MemberCommandSender)

  suspend fun validate(event: GroupMessageEvent): ValidatorOperation
}

interface MessageValidatorWithLoop: MessageValidator {
  val loopInterval: Duration
    get() = 30.toDuration(DurationUnit.SECONDS)

  suspend fun validateLoop(member: MemberCommandSender): ValidatorOperation
}

enum class ValidatorOperation {
  PASSED,

  FAILED,

  CONTINUED,
}

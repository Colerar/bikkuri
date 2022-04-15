package me.hbj.bikkuri.cmds

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import me.hbj.bikkuri.Bikkuri
import me.hbj.bikkuri.config.BUILD_BRANCH
import me.hbj.bikkuri.config.BUILD_EPOCH_TIME
import me.hbj.bikkuri.config.COMMIT_HASH
import me.hbj.bikkuri.config.MIRAI_VERSION
import me.hbj.bikkuri.config.VERSION
import me.hbj.bikkuri.data.General
import me.hbj.bikkuri.util.clearIndent
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import java.time.format.DateTimeFormatter

object Version : SimpleCommand(
  Bikkuri, "version", "版本", "v",
  description = "查看版本信息"
), RegisteredCmd {
  private val buildTime by lazy {
    val instant = Instant.fromEpochSeconds(BUILD_EPOCH_TIME)
    instant.toLocalDateTime(General.timeZone).toJavaLocalDateTime()
      .format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")) ?: "null"
  }

  @Handler
  suspend fun CommandSender.handle() {
    sendMessage(
      """
            Bikkuri Q群机器人 - $VERSION[$BUILD_BRANCH]$COMMIT_HASH
            ⏱ Built at $buildTime
            ❤️ With Kotlin ${KotlinVersion.CURRENT} & Mirai $MIRAI_VERSION
            📦 Repo at https://gitlab.com/233hbj/bikkuri
        """.clearIndent()
    )
  }
}

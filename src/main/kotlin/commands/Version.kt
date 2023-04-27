package me.hbj.bikkuri.commands

import kotlinx.datetime.Instant
import me.hbj.bikkuri.command.Command
import me.hbj.bikkuri.command.CommandSender
import me.hbj.bikkuri.config.*
import me.hbj.bikkuri.utils.Formatter.dateTime2
import me.hbj.bikkuri.utils.toFriendly

class Version(private val sender: CommandSender) : Command(Version) {
  override suspend fun run() {
    sender.sendMessage(
      """
      Bikkuri Q 群机器人 v$VERSION-$BUILD_BRANCH+$COMMIT_HASH
      ⏱️ Built at ${Instant.fromEpochSeconds(BUILD_EPOCH_TIME).toFriendly(formatter = dateTime2)}
      ❤️️ With Kotlin v${KotlinVersion.CURRENT} & Mirai $MIRAI_VERSION
      📦 Repo at $PROJECT_URL
      """.trimIndent(),
    )
  }

  companion object : Entry(
    name = "version",
    help = "打印版本信息",
  )
}

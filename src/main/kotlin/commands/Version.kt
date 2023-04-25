package me.hbj.bikkuri.commands

import me.hbj.bikkuri.command.Command
import me.hbj.bikkuri.command.CommandSender
import me.hbj.bikkuri.config.BUILD_BRANCH
import me.hbj.bikkuri.config.COMMIT_HASH
import me.hbj.bikkuri.config.VERSION

class Version(private val sender: CommandSender) : Command(Version) {
  override suspend fun run() {
    sender.sendMessage("Bikkuri v$VERSION-$BUILD_BRANCH+$COMMIT_HASH")
  }

  companion object : Entry(
    name = "version",
    help = "打印版本信息",
  )
}

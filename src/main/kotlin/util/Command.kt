package me.hbj.bikkuri.util

import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.contact.User
import java.util.Vector

private val vector = Vector<User>()

suspend fun <T : User?> T.cmdLock(action: suspend T.() -> Unit) {
  if (vector.contains(this)) return
  vector.add(this)
  action()
  vector.remove(this)
}

suspend inline fun <T : CommandSender> T.cmdLock(crossinline action: suspend T.() -> Unit) {
  this.user.cmdLock { action() }
}

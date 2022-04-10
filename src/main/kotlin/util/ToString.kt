package me.hbj.bikkuri.util

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toLocalDateTime
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.contact.remarkOrNick
import java.time.format.DateTimeFormatter

typealias jInstant = java.time.Instant
typealias jLocalDateTime = java.time.LocalDateTime

fun Group?.toString() = this?.let { "Group-$name($id)" } ?: "UnkGroup"

fun Member?.toString() = this?.let { "Member-$nameCardOrNick($id)" } ?: "UnkMember"

fun Friend?.toString() = this?.let { "Friend-$remarkOrNick($id)" } ?: "UnkFriend"

fun Member.toFriendly() = this.let { "$nameCardOrNick($id)" }

private val readableDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")

private val readableDateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")

fun jLocalDateTime.toReadDateTime(): String? {
  return format(readableDateTimeFormatter)
}

fun jLocalDateTime.toReadDate(): String? {
  return format(readableDateFormatter)
}

fun jInstant.toLocalDateTime(zone: TimeZone = TimeZone.currentSystemDefault()): jLocalDateTime {
  return toKotlinInstant().toLocalDateTime(zone).toJavaLocalDateTime()
}

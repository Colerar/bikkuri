package me.hbj.bikkuri.util

import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.contact.remarkOrNick

fun Group?.toString() = this?.let { "Group-$name($id)" } ?: "UnkGroup"

fun Member?.toString() = this?.let { "Member-$nameCardOrNick($id)" } ?: "UnkMember"

fun Friend?.toString() = this?.let { "Friend-$remarkOrNick($id)" } ?: "UnkFriend"

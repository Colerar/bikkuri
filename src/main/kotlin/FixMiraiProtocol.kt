@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package me.hbj.bikkuri

import net.mamoe.mirai.internal.utils.MiraiProtocolInternal
import net.mamoe.mirai.utils.BotConfiguration.MiraiProtocol.ANDROID_PAD
import net.mamoe.mirai.utils.BotConfiguration.MiraiProtocol.ANDROID_PHONE
import net.mamoe.mirai.utils.BotConfiguration.MiraiProtocol.IPAD
import net.mamoe.mirai.utils.BotConfiguration.MiraiProtocol.MACOS

fun fixProtoVersion() {
  MiraiProtocolInternal.protocols[ANDROID_PHONE] = MiraiProtocolInternal(
    "com.tencent.mobileqq",
    537151682,
    "8.9.33.10335",
    "6.0.0.2534",
    150470524,
    0x10400,
    16724722,
    "A6 B7 45 BF 24 A2 C2 77 52 77 16 F6 F3 6E B6 8D",
    1673599898L,
    19,
  )
  MiraiProtocolInternal.protocols[ANDROID_PAD] = MiraiProtocolInternal(
    "com.tencent.mobileqq",
    537151218,
    "8.9.33.10335",
    "6.0.0.2534",
    150470524,
    0x10400,
    16724722,
    "A6 B7 45 BF 24 A2 C2 77 52 77 16 F6 F3 6E B6 8D",
    1673599898L,
    19,
  )
  MiraiProtocolInternal.protocols[IPAD] = MiraiProtocolInternal(
    "com.tencent.minihd.qq",
    537151363,
    "8.9.33.614",
    "6.0.0.2433",
    150470524,
    66560,
    1970400,
    "AA 39 78 F4 1F D9 6F F9 91 4A 66 9E 18 64 74 C7",
    1640921786L,
    12,
  )
  MiraiProtocolInternal.protocols[MACOS] = MiraiProtocolInternal(
    "com.tencent.qq",
    537128930,
    "5.8.9",
    "6.0.0.2433",
    150470524,
    66560,
    1970400,
    "AA 39 78 F4 1F D9 6F F9 91 4A 66 9E 18 64 74 C7",
    1595836208L,
    12,
  )
}

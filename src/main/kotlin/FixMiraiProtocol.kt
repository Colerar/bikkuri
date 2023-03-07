@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package me.hbj.bikkuri

import net.mamoe.mirai.internal.utils.MiraiProtocolInternal
import net.mamoe.mirai.utils.BotConfiguration

fun fixProtoVersion() {
  MiraiProtocolInternal.protocols[BotConfiguration.MiraiProtocol.ANDROID_PHONE] = MiraiProtocolInternal(
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
  MiraiProtocolInternal.protocols[BotConfiguration.MiraiProtocol.ANDROID_PAD] = MiraiProtocolInternal(
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
}

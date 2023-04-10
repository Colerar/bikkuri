@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package me.hbj.bikkuri

import net.mamoe.mirai.internal.utils.MiraiProtocolInternal
import net.mamoe.mirai.utils.BotConfiguration.MiraiProtocol.ANDROID_PAD
import net.mamoe.mirai.utils.BotConfiguration.MiraiProtocol.ANDROID_PHONE
import net.mamoe.mirai.utils.BotConfiguration.MiraiProtocol.ANDROID_WATCH
import net.mamoe.mirai.utils.BotConfiguration.MiraiProtocol.IPAD
import net.mamoe.mirai.utils.BotConfiguration.MiraiProtocol.MACOS

fun fixProtoVersion() {
  MiraiProtocolInternal.protocols[ANDROID_PHONE] = MiraiProtocolInternal(
    apkId = "com.tencent.mobileqq",
    id = 537151682,
    ver = "8.9.33.10335",
    sdkVer = "6.0.0.2534",
    miscBitMap = 0x08f7_ff7c,
    subSigMap = 0x0001_0400,
    mainSigMap = 0x00ff_32f2,
    sign = "A6 B7 45 BF 24 A2 C2 77 52 77 16 F6 F3 6E B6 8D",
    buildTime = 1673599898L,
    ssoVersion = 19
  )
  MiraiProtocolInternal.protocols[ANDROID_PAD] = MiraiProtocolInternal(
    apkId = "com.tencent.mobileqq",
    id = 537151218,
    ver = "8.9.33.10335",
    sdkVer = "6.0.0.2534",
    miscBitMap = 0x08f7_ff7c,
    subSigMap = 0x0001_0400,
    mainSigMap = 0x00ff_32f2,
    sign = "A6 B7 45 BF 24 A2 C2 77 52 77 16 F6 F3 6E B6 8D",
    buildTime = 1673599898L,
    ssoVersion = 19
  )

  MiraiProtocolInternal.protocols[ANDROID_WATCH] = MiraiProtocolInternal(
    apkId = "com.tencent.qqlite",
    id = 537065138,
    ver = "2.0.8",
    sdkVer = "6.0.0.2365",
    miscBitMap = 0x00f7_ff7c,
    subSigMap = 0x0001_0400,
    mainSigMap = 0x00ff_32f2,
    sign = "A6 B7 45 BF 24 A2 C2 77 52 77 16 F6 F3 6E B6 8D",
    buildTime = 1559564731L,
    ssoVersion = 5
  )
  MiraiProtocolInternal.protocols[IPAD] = MiraiProtocolInternal(
    apkId = "com.tencent.minihd.qq",
    id = 537151363,
    ver = "8.9.33.614",
    sdkVer = "6.0.0.2433",
    miscBitMap = 0x08f7_ff7c,
    subSigMap = 0x0001_0400,
    mainSigMap = 0x001e_10e0,
    sign = "AA 39 78 F4 1F D9 6F F9 91 4A 66 9E 18 64 74 C7",
    buildTime = 1640921786L,
    ssoVersion = 19
  )
  MiraiProtocolInternal.protocols[MACOS] = MiraiProtocolInternal(
    apkId = "com.tencent.minihd.qq",
    id = 537128930,
    ver = "5.8.9",
    sdkVer = "6.0.0.2433",
    miscBitMap = 0x08f7_ff7c,
    subSigMap = 0x0001_0400,
    mainSigMap = 0x001e_10e0,
    sign = "AA 39 78 F4 1F D9 6F F9 91 4A 66 9E 18 64 74 C7",
    buildTime = 1595836208L,
    ssoVersion = 12
  )
}

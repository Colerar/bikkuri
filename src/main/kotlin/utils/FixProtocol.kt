@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package me.hbj.bikkuri.utils

import net.mamoe.mirai.internal.utils.MiraiProtocolInternal
import net.mamoe.mirai.utils.BotConfiguration

object FixProtocol {

  private val clazz = MiraiProtocolInternal::class.java

  private val constructor = clazz.constructors.single()

  @PublishedApi
  internal fun <T> MiraiProtocolInternal.field(name: String, default: T): T {
    @Suppress("UNCHECKED_CAST")
    return kotlin.runCatching {
      val field = clazz.getDeclaredField(name)
      field.isAccessible = true
      field.get(this) as T
    }.getOrElse {
      default
    }
  }

  @PublishedApi
  internal fun MiraiProtocolInternal.change(block: MiraiProtocolInternalBuilder.() -> Unit): MiraiProtocolInternal {
    val builder = MiraiProtocolInternalBuilder(this).apply(block)
    return when (constructor.parameterCount) {
      10 -> constructor.newInstance(
        builder.apkId,
        builder.id,
        builder.ver,
        builder.sdkVer,
        builder.miscBitMap,
        builder.subSigMap,
        builder.mainSigMap,
        builder.sign,
        builder.buildTime,
        builder.ssoVersion,
      )

      11 -> constructor.newInstance(
        builder.apkId,
        builder.id,
        builder.ver,
        builder.sdkVer,
        builder.miscBitMap,
        builder.subSigMap,
        builder.mainSigMap,
        builder.sign,
        builder.buildTime,
        builder.ssoVersion,
        builder.supportsQRLogin,
      )

      else -> this
    } as MiraiProtocolInternal
  }

  @PublishedApi
  internal class MiraiProtocolInternalBuilder(impl: MiraiProtocolInternal) {
    var apkId: String = impl.field("apkId", "")
    var id: Long = impl.field("id", 0)
    var ver: String = impl.field("ver", "")
    var sdkVer: String = impl.field("sdkVer", "")
    var miscBitMap: Int = impl.field("miscBitMap", 0)
    var subSigMap: Int = impl.field("subSigMap", 0)
    var mainSigMap: Int = impl.field("mainSigMap", 0)
    var sign: String = impl.field("sign", "")
    var buildTime: Long = impl.field("buildTime", 0)
    var ssoVersion: Int = impl.field("ssoVersion", 0)
    var supportsQRLogin: Boolean = impl.field("supportsQRLogin", false)
  }

  @JvmStatic
  fun update() {
    MiraiProtocolInternal.protocols.compute(BotConfiguration.MiraiProtocol.ANDROID_PHONE) { _, impl ->
      when (impl) {
        null -> null
        else -> impl.change {
          apkId = "com.tencent.mobileqq"
          id = 537153294
          ver = "8.9.35.10440"
          sdkVer = "6.0.0.2535"
          miscBitMap = 0x08F7_FF7C
          subSigMap = 0x0001_0400
          mainSigMap = 0x00FF_32F2
          sign = "A6 B7 45 BF 24 A2 C2 77 52 77 16 F6 F3 6E B6 8D"
          buildTime = 1676531414L
          ssoVersion = 19
        }
      }
    }
    MiraiProtocolInternal.protocols.compute(BotConfiguration.MiraiProtocol.ANDROID_PAD) { _, impl ->
      when (impl) {
        null -> null
        else -> impl.change {
          apkId = "com.tencent.mobileqq"
          id = 537152242
          ver = "8.9.35.10440"
          sdkVer = "6.0.0.2535"
          miscBitMap = 0x08F7_FF7C
          subSigMap = 0x0001_0400
          mainSigMap = 0x00FF_32F2
          sign = "A6 B7 45 BF 24 A2 C2 77 52 77 16 F6 F3 6E B6 8D"
          buildTime = 1676531414L
          ssoVersion = 19
        }
      }
    }
    MiraiProtocolInternal.protocols.compute(BotConfiguration.MiraiProtocol.ANDROID_WATCH) { _, impl ->
      when (impl) {
        null -> null
        else -> impl.change {
          apkId = "com.tencent.qqlite"
          id = 537065138
          ver = "2.0.8"
          sdkVer = "6.0.0.2365"
          miscBitMap = 0x00F7_FF7C
          subSigMap = 0x0001_0400
          mainSigMap = 0x00FF_32F2
          sign = "A6 B7 45 BF 24 A2 C2 77 52 77 16 F6 F3 6E B6 8D"
          buildTime = 1559564731L
          ssoVersion = 5
          supportsQRLogin = true
        }
      }
    }
    MiraiProtocolInternal.protocols.compute(BotConfiguration.MiraiProtocol.IPAD) { _, impl ->
      when (impl) {
        null -> null
        else -> impl.change {
          apkId = "com.tencent.minihd.qq"
          id = 537151363
          ver = "8.9.33.614"
          sdkVer = "6.0.0.2433"
          miscBitMap = 0x08F7_FF7C
          subSigMap = 0x0001_0400
          mainSigMap = 0x001E_10E0
          sign = "AA 39 78 F4 1F D9 6F F9 91 4A 66 9E 18 64 74 C7"
          buildTime = 1640921786L
          ssoVersion = 19
        }
      }
    }
    MiraiProtocolInternal.protocols.compute(BotConfiguration.MiraiProtocol.MACOS) { _, impl ->
      when (impl) {
        null -> null
        else -> impl.change {
          if (supportsQRLogin) return@change
          apkId = "com.tencent.minihd.qq"
          id = 537128930
          ver = "5.8.9"
          sdkVer = "6.0.0.2433"
          miscBitMap = 0x08F7_FF7C
          subSigMap = 0x0001_0400
          mainSigMap = 0x001E_10E0
          sign = "AA 39 78 F4 1F D9 6F F9 91 4A 66 9E 18 64 74 C7"
          buildTime = 1595836208L
          ssoVersion = 12
        }
      }
    }
  }
}
package me.hbj.bikkuri.configs

import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import me.hbj.bikkuri.persist.DataFilePersist
import me.hbj.bikkuri.utils.lenientYaml
import me.hbj.bikkuri.utils.resolveConfigDirectory
import net.mamoe.mirai.utils.BotConfiguration

object AutoLoginConfig : DataFilePersist<AutoLoginConfig.Data>(
  resolveConfigDirectory("auto-login.yml"),
  Data(),
  Data.serializer(),
  lenientYaml,
) {
  fun exampleToString(): String {
    val data = Data(
      listOf(
        Account(
          12345,
          Auth.Password,
          "114514",
        ),
      ),
    )
    return lenientYaml.encodeToString(data)
  }

  @Serializable
  data class Data(
    val accounts: List<Account> = emptyList(),
  )

  @Serializable
  data class Account(
    val account: Long,
    @YamlComment("支持的选项: Password, PasswordMd5, Qr")
    val auth: Auth = Auth.Password,
    @YamlComment("若为 Password 填明文, 若为 Md5 填 hex, Qr 不需要填")
    val password: String? = null,
    @YamlComment("登录协议: ANDROID_PHONE, ANDROID_PAD, ANDROID_WATCH, IPAD, MACOS")
    val protocol: BotConfiguration.MiraiProtocol = BotConfiguration.MiraiProtocol.ANDROID_PHONE,
  )

  enum class Auth {
    Password,
    PasswordMd5,
    Qr,
  }
}

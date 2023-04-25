package me.hbj.bikkuri.configs

import com.charleskorn.kaml.YamlComment
import kotlinx.datetime.TimeZone
import kotlinx.serialization.Serializable
import me.hbj.bikkuri.persist.DataFilePersist
import me.hbj.bikkuri.utils.lenientYaml
import me.hbj.bikkuri.utils.resolveConfigDirectory

object General : DataFilePersist<General.Data>(
  resolveConfigDirectory("general.yml"),
  Data(),
  Data.serializer(),
  lenientYaml,
) {

  @Serializable
  data class Data(
    @YamlComment("是否开启联系人缓存")
    val contactCache: Boolean = false,
    @YamlComment("是否开启登录密钥缓存")
    val loginSecretCache: Boolean = true,
    val timezone: TimeZone = TimeZone.currentSystemDefault(),
    val adminGroups: List<Long> = emptyList(),
  )
}

package me.hbj.bikkuri.bili.util.encoding

import io.ktor.util.*
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

internal interface RSA {
  fun encryptWithPublicKey(publicKey: String, data: String): String
  fun decryptWithPrivateKey(privateKey: String, data: String): String
}

internal object RSAProvider : RSA {

  private val keyFactory: KeyFactory = KeyFactory.getInstance("RSA")

  private val cipher: Cipher =
    Cipher.getInstance("RSA/ECB/PKCS1Padding") // Do not need secure mode, for external use only

  private fun getPublicKey(string: String): PublicKey {
    return keyFactory.generatePublic(X509EncodedKeySpec(string.decodeBase64Bytes()))
  }

  private fun getPrivateKey(string: String): PrivateKey {
    return keyFactory.generatePrivate(PKCS8EncodedKeySpec(string.decodeBase64Bytes()))
  }

  override fun encryptWithPublicKey(publicKey: String, data: String): String {
    cipher.init(Cipher.ENCRYPT_MODE, getPublicKey(publicKey))
    val final = cipher.doFinal(data.toByteArray())
    return final.encodeBase64()
  }

  override fun decryptWithPrivateKey(privateKey: String, data: String): String {
    cipher.init(Cipher.DECRYPT_MODE, getPrivateKey(privateKey))
    val final = cipher.doFinal(data.decodeBase64Bytes())
    return String(final)
  }
}

internal fun trimPem(string: String): String =
  Regex("""([\s\r\n]|(-.*-))""").replace(string, "")

package me.hbj.bikkuri.data

import kotlinx.serialization.Serializable

@Serializable
data class KeygenData(
  val keygen: String,
)

private val withSurround = Regex("""\[(\d+)]""")
private val pureNumber = Regex("""(\d+)""")

fun String?.fitKeygen(keygen: KeygenData): Boolean {
  if (this == null) return false
  return listOf(
    { withSurround.find(this)?.value?.removeSurrounding("[", "]") },
    { pureNumber.find(this)?.value },
  ).any {
    it() == keygen.keygen
  }
}

private const val PATTERN = "0123456789"

fun randomKeygen(length: Int): String =
  StringBuilder(length).apply {
    repeat(length) {
      append(PATTERN.random())
    }
  }.toString()

fun KeygenData(length: Int) = KeygenData(randomKeygen(length))

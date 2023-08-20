package me.hbj.bikkuri.bili.data.live

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class BatteryCurrency(
  val value: Int,
) {
  val cny: Double
    get() = (value.toDouble() / 1000.0)
}

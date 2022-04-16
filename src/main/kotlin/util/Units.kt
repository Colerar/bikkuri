package me.hbj.bikkuri.util

private const val BANDWIDTH_SCALE: Double = 1000.0

/**
 * [Bandwidth] should with decimal **SI-standard prefix**, like `k`, `M`, `G`.
 * Minimum unit are `bps`, *bits per second*, instead of bytes per second
 */
@JvmInline
value class Bandwidth(
  val bps: Long,
) {

  /**
   * `kbps`
   */
  val kbps: Double
    get() = bps / BANDWIDTH_SCALE

  /**
   * `Mbps`
   */
  val mbps: Double
    get() = kbps / BANDWIDTH_SCALE

  /**
   * `Gbps`
   */
  val gbps: Double
    get() = mbps / BANDWIDTH_SCALE

  fun toShow(): String = when {
    gbps >= 1 -> String.format("%.2f Gbps", gbps)
    mbps >= 1 -> String.format("%.1f Mbps", mbps)
    kbps >= 1 -> String.format("%.0f kbps", kbps)
    else -> String.format("%d bps", bps)
  }

  fun toBytes() = Size(bps / 8)

  // bps / (8 / 1024 * 1000)
  fun toBytesBandwidth() = BytesBandwidth((bps / 7.8125).toLong())
}

private const val SIZE_SCALE = 1024.0

@JvmInline
value class BytesBandwidth(
  val bytes: Long
) {
  /**
   * `KiB/s`
   */
  val kibPerS: Double
    get() = bytes / SIZE_SCALE

  /**
   * `MiB/s`
   */
  val mibPerS: Double
    get() = kibPerS / SIZE_SCALE

  /**
   * `GiB/s`
   */
  val gibPerS: Double
    get() = mibPerS / SIZE_SCALE

  fun toShow(): String = when {
    gibPerS >= 1 -> String.format("%.2f GiB/s", gibPerS)
    mibPerS >= 1 -> String.format("%.1f MiB/s", mibPerS)
    kibPerS >= 1 -> String.format("%.0f KiB/s", kibPerS)
    else -> String.format("%d B/s", bytes)
  }

  // 8 / 1000 * 1024
  fun toBps() = Size((bytes * 8.192).toLong())
}

/**
 * bytes, with binary unit, like `KiB` `MiB` `GiB`
 */
@JvmInline
value class Size(
  val bytes: Long,
) {
  operator fun plus(other: Size) = Size(this.bytes + other.bytes)

  operator fun minus(other: Size) = Size(this.bytes - other.bytes)

  operator fun times(other: Size) = Size(this.bytes * other.bytes)

  operator fun div(other: Size) = Size(this.bytes / other.bytes)

  /**
   * KiB
   */
  val kib: Double
    get() = bytes / SIZE_SCALE

  /**
   * MiB
   */
  val mib: Double
    get() = kib / SIZE_SCALE

  /**
   * GiB
   */
  val gib: Double
    get() = mib / SIZE_SCALE

  fun toShow(): String = when {
    gib >= 1 -> String.format("%.2f GiB", gib)
    mib >= 1 -> String.format("%.1f MiB", mib)
    kib >= 1 -> String.format("%.0f KiB", kib)
    else -> String.format("%d B", bytes)
  }

  /**
   * @param duration unit in second
   */
  fun toBandwidth(duration: Long): Bandwidth = Bandwidth(bytes * 8 / duration)
  fun toBandwidthMs(duration: Long): Bandwidth = Bandwidth(bytes * 8 * 1000 / duration)
}

package me.hbj.bikkuri.bili.util.compress

interface ICompress {
  suspend fun compress(data: ByteArray): ByteArray
  suspend fun decompress(data: ByteArray): ByteArray
}

package me.hbj.bikkuri.util

import com.aayushatharva.brotli4j.Brotli4jLoader
import com.aayushatharva.brotli4j.decoder.BrotliInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.sdl.yabapi.util.compress.ICompress

object BrotliImpl : ICompress {
  init {
    Brotli4jLoader.ensureAvailability()
  }

  override suspend fun compress(data: ByteArray): ByteArray = withContext(Dispatchers.Default) {
    // val params = Encoder.Parameters().apply {
    //   setQuality(-1)
    //   setMode(Encoder.Mode.TEXT)
    // }
    TODO()
  }

  override suspend fun decompress(data: ByteArray): ByteArray = withContext(Dispatchers.Default) {
    BrotliInputStream(data.inputStream()).buffered().use {
      it.readAllBytes()
    }
  }
}

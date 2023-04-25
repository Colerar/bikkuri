package me.hbj.bikkuri.utils

import java.io.File

private val logger = mu.KotlinLogging.logger { }

/**
 * Global Work Directory, set by `user.dir`
 */
val globalWorkDirectory by lazyPublication {
  val workDir = testDir ?: System.getProperty("user.dir") ?: error("Failed to get property 'user.dir'")

  File(workDir)
}

val configDirectory by lazyPublication {
  resolveWorkDirectory("config")
}

val cacheDirectory by lazyPublication {
  resolveWorkDirectory("cache")
}

val dataDirectory by lazyPublication {
  resolveWorkDirectory("data")
}

val resourceDirectory by lazyPublication {
  resolveWorkDirectory("resource")
}

private val testDir by lazyPublication {
  runCatching {
    Class.forName("me.hbj.bikkuri.persist.TestConfig")
      .getDeclaredField("TEST_DIR")
      .get(null) as String
  }.getOrNull()
}

fun resolveHome(path: String): File? = System.getProperty("user.home")?.let { File(it, path) }

/**
 * Resolve work dir
 */
fun resolveWorkDirectory(path: String) = File(globalWorkDirectory, path)

fun resolveConfigDirectory(path: String) = File(configDirectory, path)

fun resolveDataDirectory(path: String) = File(dataDirectory, path)

fun resolveResourceDirectory(path: String) = File(resourceDirectory, path)

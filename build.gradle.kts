import com.diffplug.gradle.spotless.FormatExtension
import com.github.gmazzo.gradle.plugins.BuildConfigSourceSet
import org.apache.commons.io.IOUtils
import org.gradle.internal.os.OperatingSystem
import java.net.URL
import java.nio.file.Files
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

group = "me.hbj.bikkuri"
version = "2.2.0"

plugins {
  kotlin("jvm") version libs.versions.kotlin
  application
  alias(libs.plugins.spotless)
  alias(libs.plugins.shadow)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.buildconfig)
}

repositories {
  mavenCentral()
  flatDir {
    dirs(project.layout.buildDirectory.get().asFile.resolve("download"))
  }
}

fun urlFile(url: String, name: String): ConfigurableFileCollection {
  val download = project.layout.buildDirectory.get().asFile.resolve("download")
  download.mkdirs()
  val file = download.resolve(name)
  if (!file.exists()) {
    file.createNewFile()
    URL(url).openStream().buffered().use { input ->
      file.outputStream().buffered().use { output ->
        IOUtils.copyLarge(input, output)
      }
    }
  }
  return files(file.absolutePath)
}

dependencies {
  // Logger
  implementation(libs.bundles.log)
  // Bilibili
  implementation(libs.yabapi)
  // Coroutines
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.atomicfu)
  // Mirai
  implementation(libs.mirai.core.api)
  implementation(libs.mirai.core.utils)
  implementation(libs.mirai.core)
  implementation(libs.qsign)
//  implementation("org.asynchttpclient:async-http-client:2.12.3")

//  urlFile(
//    "https://github.com/cssxsh/fix-protocol-version/releases/download/v1.10.0/fix-protocol-version-1.10.0.mirai2.jar",
//    "fix-protocol-version-1.10.0.mirai2.jar"
//  )
//  implementation(fileTree(mapOf(
//    "dir" to "build/download",
//    "include" to listOf("*.jar")
//  )))
  runtimeOnly(libs.mirai.log.logback)
  // Command
  implementation(libs.yac)
  // Datetime
  implementation(libs.kotlinx.datetime)
  // Serialization
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.kaml)
  // Ktor
  implementation(libs.ktor.client.core)
  implementation(libs.ktor.client.okhttp)
  implementation(libs.ktor.client.encoding)
  implementation(libs.ktor.client.content.negotiation)
  implementation(libs.ktor.serialization.kotlinx.json)
  // IO
  implementation(libs.okio)
  // Database
  implementation(libs.sqlite)
  implementation(libs.hikaricp)
  implementation(libs.exposed.core)
  implementation(libs.exposed.dao)
  implementation(libs.exposed.jdbc)
  implementation(libs.exposed.java.time)
  // Cron
  implementation(libs.krontab)
  // CSV
  implementation(libs.doyaaaaaken.csv)
  // Test
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
  testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.0")
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(17))
  }
}

application {
  mainClass.set("me.hbj.bikkuri.MainKt")
}

tasks.named<Test>("test") {
  useJUnitPlatform()
}

spotless {
  fun FormatExtension.excludes() {
    targetExclude("**/build/", "**/generated/", "**/resources/")
  }

  fun FormatExtension.common() {
    trimTrailingWhitespace()
    lineEndings = com.diffplug.spotless.LineEnding.UNIX
    endWithNewline()
  }

  val ktlintConfig = mapOf(
    "ij_kotlin_allow_trailing_comma" to "true",
    "ij_kotlin_allow_trailing_comma_on_call_site" to "true",
    "trailing-comma-on-declaration-site" to "true",
    "trailing-comma-on-call-site" to "true",
    "ktlint_standard_no-wildcard-imports" to "disabled",
    "ktlint_disabled_import-ordering" to "disabled",
  )

  kotlin {
    target("**/*.kt")
    excludes()
    common()
    ktlint(libs.versions.ktlint.get()).editorConfigOverride(ktlintConfig)
  }

  kotlinGradle {
    target("**/*.gradle.kts")
    excludes()
    common()
    ktlint(libs.versions.ktlint.get()).editorConfigOverride(ktlintConfig)
  }
}

fun Project.installGitHooks() {
  val git = File(project.rootProject.rootDir, ".git")
  val target = File(project.rootProject.rootDir, ".git/hooks")
  val source = File(project.rootProject.rootDir, ".git-hooks")
  if (!git.exists() || !source.exists()) return
  if (target.canonicalFile == source) return
  target.deleteRecursively()
  if (OperatingSystem.current().isWindows) {
    // Windows requires Admin permission for creating symlinks.
    source.copyRecursively(target)
  } else {
    Files.createSymbolicLink(target.toPath(), source.toPath())
  }
}
installGitHooks()

val commitHash by lazy {
  val p = ProcessBuilder("git", "rev-parse", "--short", "HEAD").start()
  val commit = p.inputStream.bufferedReader().readLine() ?: "UnkCommit"
  p.waitFor(5, TimeUnit.SECONDS)
  commit
}

val branch by lazy {
  val p = ProcessBuilder("git", "rev-parse", "--abbrev-ref", "HEAD").start()
  val branch = p.inputStream.bufferedReader().readLine() ?: "UnkBranch"
  p.waitFor(5, TimeUnit.SECONDS)
  branch
}

val time: String by lazy {
  ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_ZONED_DATE_TIME)
}

val epochTime: Long by lazy {
  ZonedDateTime.now(ZoneOffset.UTC).toEpochSecond()
}

fun BuildConfigSourceSet.string(name: String, value: String) = buildConfigField("String", name, "\"$value\"")
fun BuildConfigSourceSet.stringNullable(name: String, value: String?) =
  buildConfigField("String?", name, value?.let { "\"$value\"" } ?: "null")

fun BuildConfigSourceSet.long(name: String, value: Long) = buildConfigField("long", name, value.toString())
fun BuildConfigSourceSet.longNullable(name: String, value: Long?) =
  buildConfigField("Long?", name, value?.let { "$value" } ?: "null")

buildConfig {
  packageName("$group.config")
  useKotlinOutput { topLevelConstants = true }
  string("VERSION", version.toString())
  string("NAME", rootProject.name)
  string("BUILD_BRANCH", branch)
  string("COMMIT_HASH", commitHash)
  string("PROJECT_URL", "https://github.com/Colerar/bikkuri")
  long("BUILD_EPOCH_TIME", epochTime)
  string("MIRAI_VERSION", libs.versions.mirai.get())

  sourceSets["test"].apply {
    string("TEST_DIR", rootProject.rootDir.absolutePath)
  }
}

tasks.shadowJar {
  exclude("net/mamoe/mirai/internal/deps/io/ktor/")
  exclude("net/mamoe/mirai/internal/deps/okio/")
  exclude("net/mamoe/mirai/internal/deps/okhttp3/")
  relocate("net/mamoe/mirai/internal/deps/io/ktor/", "io/ktor/")
  relocate("net/mamoe/mirai/internal/deps/okio/", "io/okio/")
  relocate("net/mamoe/mirai/internal/deps/okhttp3/", "okhttp3/")
  exclude("checkstyle.xml")
  exclude("**/*.html")
  exclude("DebugProbesKt.bin")
  exclude("win32-x86/**/*")
  exclude("natives/windows_32/**/*")
  exclude("**/libdynarmic.dylib")
  exclude("**/libdynarmic.so")
  exclude("**/dynarmic.dll")
  exclude("org/sqlite/native/FreeBSD/**/*")
  exclude("org/sqlite/native/Linux-Android/**/*")
  exclude("org/sqlite/native/Linux-Musl/**/*")
  listOf("arm", "armv6", "armv7", "ppc64", "x86").forEach {
    exclude("org/sqlite/native/Linux/$it/**/*")
    exclude("org/sqlite/native/Windows/$it/**/*")
  }
  listOf("freebsd32", "freebsd64", "linux32", "windows32").forEach {
    exclude("META-INF/native/$it/**/*")
  }
  listOf("aix", "freebsd", "openbsd", "sunos").forEach {
    exclude("com/sun/jna/$it*/**/*")
  }
  listOf("arm", "armel", "loongarch64", "mips64el", "ppc", "ppc64le", "riscv64", "s390x", "x86").forEach {
    exclude("com/sun/jna/linux-$it/**/*")
    exclude("com/sun/jna/win32-$it/**/*")
  }
}

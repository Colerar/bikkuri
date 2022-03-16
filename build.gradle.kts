import com.github.gmazzo.gradle.plugins.BuildConfigSourceSet
import org.gradle.nativeplatform.platform.internal.ArchitectureInternal
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import org.gradle.nativeplatform.platform.internal.DefaultOperatingSystem
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Properties

plugins {
  application
  kotlin("jvm")
  kotlin("plugin.serialization")
  id("com.github.gmazzo.buildconfig")
  id("org.jlleitschuh.gradle.ktlint")
  id("org.jlleitschuh.gradle.ktlint-idea")
  id("com.github.johnrengelman.shadow")
}

group = "me.hbj.bikkuri"
version = "0.4.0"

repositories {
  mavenCentral()
  maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
  maven("https://maven.aliyun.com/repository/public")
}

configurations.all {
  resolutionStrategy.cacheChangingModulesFor(0, "minutes")
}

val prop = Properties().apply {
  val file = project.rootProject.file("local.properties")
  if (file.exists()) load(file.inputStream())
}

val hostOs: DefaultOperatingSystem = DefaultNativePlatform.getCurrentOperatingSystem()

val hostArch: ArchitectureInternal = DefaultNativePlatform.getCurrentArchitecture()

val targets by lazy {
  when {
    prop["brotli.target"] != null -> prop["brotli.target"].toString().split(",").toTypedArray()
    hostOs.isWindows -> arrayOf("windows-x86_64")
    hostOs.isMacOsX -> arrayOf("osx-x86_64")
    hostOs.isLinux -> when {
      hostArch.isArm -> arrayOf("linux-aarch64")
      else -> arrayOf("linux-x86_64")
    }
    else -> error("unsupported target for ${hostOs.name}/$hostArch")
  }
}

var versions = Properties().apply {
  load(project.rootProject.file("versions.properties").inputStream())
}

dependencies {
  implementation("net.mamoe:mirai-core:_")
  implementation("net.mamoe:mirai-console:_")
  implementation("net.mamoe:mirai-console-terminal:_")
  // Logger
  implementation("net.mamoe:mirai-logging-slf4j-logback:_")
  implementation("io.github.microutils:kotlin-logging-jvm:_")
  implementation("ch.qos.logback:logback-core:_")
  implementation("ch.qos.logback:logback-classic:_")
  // Kotlinx
  implementation(KotlinX.datetime)
  implementation(KotlinX.coroutines.core)
  implementation(KotlinX.Serialization.core)
  implementation(KotlinX.Serialization.json)
  implementation("org.jetbrains.kotlinx:atomicfu:_")
  // BiliBili
  implementation("moe.sdl.yabapi:yabapi-core-jvm:_")
  // Ktor
  implementation(Ktor.client.core)
  implementation(Ktor.client.cio)
  implementation(Ktor.client.websockets)
  implementation(Ktor.client.encoding)
  // IO
  implementation(Square.okio)
  // Brotli
  implementation("com.aayushatharva.brotli4j:brotli4j:_")
  val brotli4jVer = versions["version.com.aayushatharva.brotli4j..brotli4j"]
  targets.forEach {
    implementation("com.aayushatharva.brotli4j:native-$it:$brotli4jVer")
  }
  // Test framework
  testImplementation(Testing.junit.jupiter.api)
  testImplementation(Testing.junit.jupiter.engine)
  testImplementation(Kotlin.test.junit5)
}

val commitHash by lazy {
  val commitHashCommand = "git rev-parse --short HEAD"
  Runtime.getRuntime().exec(commitHashCommand).inputStream.bufferedReader().readLine() ?: "UnkCommit"
}

val branch by lazy {
  val branchCommand = "git rev-parse --abbrev-ref HEAD"
  Runtime.getRuntime().exec(branchCommand).inputStream.bufferedReader().readLine() ?: "UnkBranch"
}

val time: String by lazy {
  ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_ZONED_DATE_TIME)
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
  string("MAIN_GROUP", group.toString())
  string("BUILD_BRANCH", branch)
  string("BUILD_TIME", time)
  string("VERSION_LONG", "$version-[$branch]$commitHash $time")
  string("MIRAI_VERSION", versions["version.net.mamoe..mirai-core"]?.toString() ?: "unk")

  sourceSets["test"].apply {
    string("TEST_DIR", rootProject.rootDir.absolutePath)
  }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
  kotlinOptions.jvmTarget = "17"
  kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
}

tasks.withType<Test> {
  useJUnitPlatform()
}

application {
  mainClass.set("me.hbj.bikkuri.MainKt")
}

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
  reporters {
    reporter(ReporterType.HTML)
    reporter(ReporterType.CHECKSTYLE)
  }
}

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

val hostOs: DefaultOperatingSystem = DefaultNativePlatform.getCurrentOperatingSystem()

val hostArch: ArchitectureInternal = DefaultNativePlatform.getCurrentArchitecture()

val target by lazy {
  when {
    hostOs.isWindows -> "windows-x86_64"
    hostOs.isMacOsX -> "osx-x86_64"
    hostOs.isLinux -> when {
      hostArch.isArm -> "linux-aarch64"
      else -> "linux-x86_64"
    }
    else -> ""
  }
}

dependencies {
  api("net.mamoe:mirai-logging-log4j2:_")
  implementation("net.mamoe:mirai-core:_")
  implementation("net.mamoe:mirai-console:_")
  implementation("net.mamoe:mirai-console-terminal:_")
  fun Log4J(artifact: String) = "org.apache.logging.log4j:$artifact:_"
  implementation(Log4J("log4j-api"))
  implementation(Log4J("log4j-core"))
  implementation(Log4J("log4j-slf4j-impl"))
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
  implementation("com.aayushatharva.brotli4j:native-$target:_")
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

  val version = Properties().apply {
    load(project.rootProject.file("versions.properties").inputStream())
  }

  string("MIRAI_VERSION", version["version.net.mamoe..mirai-core"]?.toString() ?: "unk")

  sourceSets["test"].apply {
    val prop = Properties().apply {
      load(project.rootProject.file("local.properties").inputStream())
    }
    val id: Long? = prop["bikkuri.test.id"]?.toString()?.toLongOrNull()
    val pwd: String? = prop["bikkuri.test.pwd"]?.toString()
    longNullable("TEST_ID", id)
    stringNullable("TEST_PWD", pwd)
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

import com.github.gmazzo.gradle.plugins.BuildConfigSourceSet
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Properties

plugins {
  kotlin("jvm")
  kotlin("plugin.serialization")
  id("net.mamoe.mirai-console")
  id("com.github.gmazzo.buildconfig")
  id("org.jlleitschuh.gradle.ktlint")
  id("org.jlleitschuh.gradle.ktlint-idea")
}

group = "me.hbj.bikkuri"
version = "0.2.0-DEV"

repositories {
  mavenCentral()
  maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
  maven("https://maven.aliyun.com/repository/public")
}

dependencies {
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
  implementation(Ktor.client.encoding)
  // IO
  implementation(Square.okio)
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

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {

  reporters {
    reporter(ReporterType.HTML)
    reporter(ReporterType.CHECKSTYLE)
  }
}

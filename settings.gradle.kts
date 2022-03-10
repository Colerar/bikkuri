rootProject.name = "Bikkuri"

pluginManagement {
  repositories {
    gradlePluginPortal()
    mavenCentral()
    maven("https://s01.oss.sonatype.org/content/repositories/snapshot")
  }
}

plugins {
  id("de.fayard.refreshVersions") version "0.40.1"
}

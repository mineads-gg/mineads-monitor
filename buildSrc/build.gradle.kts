plugins {
  `kotlin-dsl`
}

repositories {
  gradlePluginPortal()
  mavenCentral()
}

dependencies {
  implementation("com.gradleup.shadow:shadow-gradle-plugin:9.1.0")
  implementation("com.diffplug.spotless:spotless-plugin-gradle:7.2.1")
  implementation("com.github.spotbugs.snom:spotbugs-gradle-plugin:6.2.7")
  implementation("net.kyori:indra-git:3.2.0")
  implementation("xyz.wagyourtail.jvmdowngrader:xyz.wagyourtail.jvmdowngrader.gradle.plugin:1.3.3")
  implementation("net.ltgt.errorprone:net.ltgt.errorprone.gradle.plugin:4.3.0")
  implementation("io.freefair.gradle:lombok-plugin:8.14.2")
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(17))
  }
}

kotlin {
  jvmToolchain(17)
}

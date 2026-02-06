plugins {
  `kotlin-dsl`
}

repositories {
  gradlePluginPortal()
  mavenCentral()
  maven("https://repo.papermc.io/repository/maven-public/") {
    name = "PaperMC"
  }
}

dependencies {
  implementation("com.gradleup.shadow:shadow-gradle-plugin:9.3.1")
  implementation("com.diffplug.spotless:spotless-plugin-gradle:8.1.0")
  implementation("com.github.spotbugs.snom:spotbugs-gradle-plugin:6.4.8")
  implementation("net.kyori:indra-git:4.0.0")
  implementation("xyz.wagyourtail.jvmdowngrader:xyz.wagyourtail.jvmdowngrader.gradle.plugin:1.3.5")
  implementation("net.ltgt.errorprone:net.ltgt.errorprone.gradle.plugin:5.0.0")
  implementation("org.openrewrite:plugin:7.25.0")
  implementation("io.freefair.gradle:lombok-plugin:9.1.0")
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(25))
  }
}

kotlin {
  jvmToolchain(25)
}

plugins {
  `java-library`
  `java-test-fixtures`
  `maven-publish`
  id("mm.formatting-logic")
  id("net.kyori.indra.git")
  id("io.freefair.lombok")
  id("net.ltgt.errorprone")
  id("com.github.spotbugs")
}

spotbugs {
  ignoreFailures = true
}

dependencies {
  api("org.jetbrains:annotations:26.0.2-1")
  compileOnly("com.github.spotbugs:spotbugs-annotations:4.9.8")

  errorprone("com.google.errorprone:error_prone_core:2.45.0")
  spotbugs("com.github.spotbugs:spotbugs:4.9.8")

  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
  testFixturesApi("org.junit.jupiter:junit-jupiter:6.0.1")
  testFixturesApi("org.mockito:mockito-core:5.21.0")
  testFixturesApi("org.mockito:mockito-junit-jupiter:5.21.0")
}

tasks {
  withType<com.github.spotbugs.snom.SpotBugsTask>().configureEach {
    val spotbugsExcludeFile = rootProject.file("config/spotbugs/exclude.xml")
    if (spotbugsExcludeFile.exists()) {
      excludeFilter.set(spotbugsExcludeFile)
    }
  }

  processResources {
    inputs.property("version", project.version)
    inputs.property("description", project.description)
    filesMatching(
      listOf(
        "plugin.yml",
        "bungee.yml",
        "velocity-plugin.json",
        "mineadsmonitor-build-data.properties"
      )
    ) {
      expand(
        mapOf(
          "version" to inputs.properties["version"],
          "description" to inputs.properties["description"],
          "url" to "https://modrinth.com/plugin/mineads-monitor",
        )
      )
    }
  }
  test {
    reports.junitXml.required = true
    reports.html.required = true
    useJUnitPlatform()
    maxParallelForks = Runtime.getRuntime().availableProcessors().div(2).coerceAtLeast(1)
  }
  jar {
    from(rootProject.file("LICENSE"))
  }
  javadoc {
    title = "MineAdsMonitor Javadocs"
    options.encoding = Charsets.UTF_8.name()
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
    onlyIf { project.name.contains("api") }
  }
  delombok {
    onlyIf { project.name.contains("api") }
  }
  compileJava {
    options.encoding = Charsets.UTF_8.name()
    options.compilerArgs.addAll(
      listOf(
        "-parameters",
        "-nowarn",
        "-Xlint:-deprecation",
        "-Xlint:-processing"
      )
    )
    options.isFork = true
  }
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(21))
  }
}

val javaComponent = components["java"] as AdhocComponentWithVariants
javaComponent.withVariantsFromConfiguration(configurations["testFixturesApiElements"]) { skip() }
javaComponent.withVariantsFromConfiguration(configurations["testFixturesRuntimeElements"]) { skip() }

publishing {
  repositories {
    maven("https://maven.pkg.github.com/mineads-gg/mineads-monitor") {
      name = "GitHubPackages"
      credentials {
        username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
        password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
      }
    }
  }
  publications {
    register<MavenPublication>("gpr") {
      from(components["java"])
      pom {
        name = "MineAdsMonitor"
        description = rootProject.description
        url = "https://github.com/mineads-gg/mineads-monitor"
        organization {
          name = "MineAds"
          url = "https://mineads.gg"
        }
        developers {
          developer {
            id = "AlexProgrammerDE"
            timezone = "Europe/Berlin"
            url = "https://pistonmaster.net"
          }
        }
        licenses {
          license {
            name = "GNU General Public License v3.0"
            url = "https://www.gnu.org/licenses/gpl-3.0.html"
          }
        }
        scm {
          connection = "scm:git:https://github.com/mineads-gg/mineads-monitor.git"
          developerConnection = "scm:git:ssh://git@github.com/mineads-gg/mineads-monitor.git"
          url = "https://github.com/mineads-gg/mineads-monitor"
        }
        ciManagement {
          system = "GitHub Actions"
          url = "https://github.com/mineads-gg/mineads-monitor/actions"
        }
        issueManagement {
          system = "GitHub"
          url = "https://github.com/mineads-gg/mineads-monitor/issues"
        }
      }
    }
  }
}

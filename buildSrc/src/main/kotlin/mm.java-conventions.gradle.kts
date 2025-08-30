plugins {
  `java-library`
  `maven-publish`
}

dependencies {
  compileOnly("org.projectlombok:lombok:1.18.38")
  annotationProcessor("org.projectlombok:lombok:1.18.38")

  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
  testImplementation("org.junit.jupiter:junit-jupiter:5.13.4")
  testImplementation("org.mockito:mockito-core:5.19.0")
  testImplementation("org.mockito:mockito-junit-jupiter:5.19.0")
}

tasks {
  processResources {
    filesMatching(
      listOf(
        "plugin.yml",
        "bungee.yml",
        "velocity-plugin.json",
        "mineadsmonitor-build-data.properties"
      )
    ) {
      expand(mapOf(
        "version" to project.version,
        "description" to project.description,
        "url" to "https://modrinth.com/plugin/mineadsmonitor",
      ))
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
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(21))
  }
}

tasks.withType<JavaCompile> {
  options.encoding = "UTF-8"
  options.compilerArgs.add("-Xlint:all,-serial,-processing")
}

tasks.withType<Javadoc> {
  enabled = false
}

publishing {
  repositories {
    maven("https://maven.pkg.github.com/mineads-gg/mineads-plugin") {
      name = "GitHubPackages"
      credentials {
        username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
        password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
      }
    }
  }
  publications {
    register<MavenPublication>("gpr") {
      from(components["java"])
      pom {
        name = "MineAdsMonitor"
        description = rootProject.description
        url = "https://github.com/mineads-gg/mineads-plugin"
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
            name = "Apache License 2.0"
            url = "https://www.apache.org/licenses/LICENSE-2.0"
          }
        }
        scm {
          connection = "scm:git:https://github.com/mineads-gg/mineads-plugin.git"
          developerConnection = "scm:git:ssh://git@github.com/mineads-gg/mineads-plugin.git"
          url = "https://github.com/mineads-gg/mineads-plugin"
        }
        ciManagement {
          system = "GitHub Actions"
          url = "https://github.com/mineads-gg/mineads-plugin/actions"
        }
        issueManagement {
          system = "GitHub"
          url = "https://github.com/mineads-gg/mineads-plugin/issues"
        }
      }
    }
  }
}

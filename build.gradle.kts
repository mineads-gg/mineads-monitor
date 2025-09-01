plugins {
  base
}

allprojects {
  group = "gg.mineads"
  version = property("maven_version")!!
  description = "Push your server analytics to MineAds."

  repositories {
    mavenCentral()
    maven("https://central.sonatype.com/repository/maven-snapshots/") {
      name = "Sonatype"
      mavenContent {
        snapshotsOnly()
      }
    }
    maven("https://repo.papermc.io/repository/maven-public/") {
      name = "PaperMC"
    }
    maven("https://repo.codemc.org/repository/maven-public") {
      name = "CodeMC"
    }
    maven("https://jitpack.io") {
      name = "jitpack.io"
    }
    maven("https://repo.tcoded.com/releases") {
      name = "tcoded-releases"
    }
  }
}

tasks.register("outputVersion") {
  doLast {
    println(project.version)
  }
}

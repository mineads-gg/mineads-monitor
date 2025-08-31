plugins {
  id("mm.platform-conventions")
  id("mm.shadow-conventions")
  id("xyz.jpenilla.run-velocity") version "2.3.1"
}

dependencies {
  implementation(project(":mineadsmonitor-api", "shadow"))
  implementation(projects.mineadsmonitorShared)
  compileOnly(projects.mineadsmonitorBuildData)

  implementation("org.bstats:bstats-velocity:3.1.0")

  compileOnly("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
}

tasks {
  runVelocity {
    version("3.4.0-SNAPSHOT")
  }
}

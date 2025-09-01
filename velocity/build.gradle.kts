plugins {
  id("mm.platform-conventions")
  id("mm.shadow-conventions")
  id("xyz.jpenilla.run-velocity") version "2.3.1"
}

dependencies {
  implementation(projects.mineadsmonitorShared)

  implementation("org.bstats:bstats-velocity:3.1.0")

  compileOnly("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
  compileOnly("net.luckperms:api:5.5")

  implementation("org.incendo:cloud-velocity:2.0.0-beta.11")
}

tasks {
  runVelocity {
    version("3.4.0-SNAPSHOT")
  }
}

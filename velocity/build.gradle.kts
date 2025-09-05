plugins {
  id("mm.platform-conventions")
  id("mm.shadow-conventions")
  alias(libs.plugins.run.velocity)
}

dependencies {
  implementation(projects.mineadsmonitorShared)
  implementation(libs.protobuf.java)

  implementation(libs.bstats.velocity)

  compileOnly(libs.velocity.api)
  compileOnly(libs.luckperms.api)

  implementation(libs.cloud.velocity)
}

tasks {
  runVelocity {
    version("3.4.0-SNAPSHOT")
  }
}

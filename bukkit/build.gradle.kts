plugins {
  id("mm.platform-conventions")
  id("mm.kyori-conventions")
  alias(libs.plugins.run.paper)
}

dependencies {
  implementation(projects.mineadsmonitorApi)
  implementation(projects.mineadsmonitorShared)

  implementation(libs.paperlib)
  implementation(libs.bstats.bukkit)
  implementation(libs.adventure.platform.bukkit)
  implementation(libs.folia.lib)

  compileOnly(libs.paper.api)
  compileOnly(libs.luckperms.api)

  implementation(libs.cloud.paper)
}

runPaper {
  folia {
    registerTask {
      minecraftVersion("1.21.8")

      jvmArgs = listOf("-Dcom.mojang.eula.agree=true")
      args = listOf("--nogui")
    }
  }
}

tasks {
  runServer {
    minecraftVersion("1.21.8")
    jvmArgs = listOf("-Dcom.mojang.eula.agree=true")
  }
}

plugins {
  id("mm.platform-conventions")
  id("mm.kyori-conventions")
  id("xyz.jpenilla.run-paper") version "2.3.1"
}

dependencies {
  implementation(projects.mineadsmonitorApi)
  implementation(projects.mineadsmonitorShared)

  implementation("io.papermc:paperlib:1.0.8")
  implementation("org.bstats:bstats-bukkit:3.1.0")
  implementation("net.kyori:adventure-platform-bukkit:4.4.1")
  implementation("com.tcoded:FoliaLib:0.5.1")

  compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
  compileOnly("net.luckperms:api:5.5")

  implementation("org.incendo:cloud-paper:2.0.0-beta.11")
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

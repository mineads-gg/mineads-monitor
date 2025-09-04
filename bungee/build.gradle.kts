plugins {
  id("mm.platform-conventions")
  id("mm.kyori-conventions")
}

dependencies {
  implementation(projects.mineadsmonitorApi)
  implementation(projects.mineadsmonitorShared)

  implementation(libs.bstats.bungeecord)
  implementation(libs.adventure.platform.bungeecord)

  compileOnly(libs.bungeecord.api)
  compileOnly(libs.luckperms.api)

  implementation(libs.cloud.bungee)
}

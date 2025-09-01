plugins {
  id("mm.platform-conventions")
  id("mm.kyori-conventions")
}

dependencies {
  implementation(projects.mineadsmonitorApi)
  implementation(projects.mineadsmonitorShared)

  implementation("org.bstats:bstats-bungeecord:3.1.0")
  implementation("net.kyori:adventure-platform-bungeecord:4.4.1")

  compileOnly("net.md-5:bungeecord-api:1.21-R0.3")
  compileOnly("net.luckperms:api:5.5")

  implementation("org.incendo:cloud-bungee:2.0.0-beta.11")
}

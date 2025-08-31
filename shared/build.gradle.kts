plugins {
  id("mm.java-conventions")
  id("mm.shadow-conventions")
}

dependencies {
  implementation(project(":mineadsmonitor-api", "shadow"))

  compileOnly("net.luckperms:api:5.5")

  implementation("de.exlll:configlib-yaml:4.6.1")
  implementation("org.msgpack:msgpack-core:0.9.10")
  implementation("com.google.code.gson:gson:2.13.1")
}

plugins {
  id("mm.java-conventions")
  id("mm.shadow-conventions")
}

dependencies {
  implementation(projects.mineadsmonitorApi)
  implementation(projects.mineadsmonitorBuildData)

  compileOnly("net.luckperms:api:5.5")

  implementation("de.exlll:configlib-yaml:4.6.1")
  implementation("org.msgpack:msgpack-core:0.9.10")
  implementation("com.google.code.gson:gson:2.13.1")

  implementation("org.incendo:cloud-core:2.0.0")
  implementation("org.incendo:cloud-annotations:2.0.0")
  annotationProcessor("org.incendo:cloud-annotations:2.0.0")

  implementation("net.kyori:adventure-api:4.24.0")
  implementation("net.kyori:adventure-text-serializer-legacy:4.24.0")
}

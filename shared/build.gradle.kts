plugins {
  id("mm.java-conventions")
  id("mm.shadow-conventions")
}

dependencies {
  implementation(projects.mineadsmonitorApi)
  implementation(projects.mineadsmonitorBuildData)

  compileOnly(libs.luckperms.api)
  testImplementation(libs.luckperms.api)

  implementation(libs.configlib.yaml)
  implementation(libs.msgpack.core)
  implementation(libs.gson)

  implementation(libs.cloud.core)
  implementation(libs.cloud.annotations)
  annotationProcessor(libs.cloud.annotations)

  implementation(libs.adventure.api)
  implementation(libs.adventure.text.serializer.legacy)
}

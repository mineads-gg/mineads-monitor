plugins {
  id("mm.java-conventions")
  id("mm.shadow-conventions")
  id("com.google.protobuf") version "0.9.6"
}

dependencies {
  implementation(projects.mineadsmonitorApi)
  implementation(projects.mineadsmonitorBuildData)

  compileOnly(libs.luckperms.api)
  testImplementation(libs.luckperms.api)

  implementation(libs.configlib.yaml)
  compileOnly(libs.gson)
  compileOnly(libs.spotbugs.annotations)
  implementation(libs.protobuf.java)

  implementation(libs.cloud.core)
  implementation(libs.cloud.annotations)
  annotationProcessor(libs.cloud.annotations)
  implementation(libs.cloud.brigadier)
  implementation(libs.cloud.translations.core)
  implementation(libs.cloud.minecraft.extras)
  implementation(libs.cloud.translations.minecraft.extras)

  compileOnly(libs.adventure.api)
  compileOnly(libs.adventure.text.serializer.legacy)
}

protobuf {
  protoc {
    artifact = "com.google.protobuf:protoc:${libs.versions.protobuf.get()}"
  }
}

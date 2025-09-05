plugins {
  id("mm.java-conventions")
  id("mm.shadow-conventions")
  id("com.google.protobuf") version "0.9.5"
}

dependencies {
  implementation(projects.mineadsmonitorApi)
  implementation(projects.mineadsmonitorBuildData)

  compileOnly(libs.luckperms.api)
  testImplementation(libs.luckperms.api)

  implementation(libs.configlib.yaml)
  implementation(libs.gson)
  implementation(libs.protobuf.java)

  implementation(libs.cloud.core)
  implementation(libs.cloud.annotations)
  annotationProcessor(libs.cloud.annotations)
  implementation(libs.cloud.brigadier)
  implementation(libs.cloud.translations.core)
  implementation(libs.cloud.minecraft.extras)
  implementation(libs.cloud.translations.minecraft.extras)

  implementation(libs.adventure.api)
  implementation(libs.adventure.text.serializer.legacy)
}

protobuf {
  protoc {
    artifact = "com.google.protobuf:protoc:4.32.0"
  }
  generateProtoTasks {
    all().forEach { task ->
      task.builtins {
        java {
        }
      }
    }
  }
}

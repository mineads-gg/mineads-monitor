import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
  id("mm.java-conventions")
  id("com.gradleup.shadow")
}

tasks {
  jar {
    archiveClassifier.set("unshaded")
    from(project.rootProject.file("LICENSE"))
  }

  shadowJar {
    exclude("META-INF/SPONGEPO.SF", "META-INF/SPONGEPO.DSA", "META-INF/SPONGEPO.RSA")
    configureRelocations()
    mergeServiceFiles()
  }

  build {
    dependsOn(shadowJar)
  }
}

fun ShadowJar.configureRelocations() {
  relocate("io.papermc.lib", "gg.mineads.monitor.shadow.paperlib")
  relocate("org.bstats", "gg.mineads.monitor.shadow.bstats")
  relocate("org.snakeyaml", "gg.mineads.monitor.shadow.snakeyaml")
  relocate("org.intellij.lang.annotations", "gg.mineads.monitor.shadow.annotations.intellij")
  relocate("org.jetbrains.annotations", "gg.mineads.monitor.shadow.annotations.jetbrains")
  relocate("org.apiguardian", "gg.mineads.monitor.shadow.annotations.apiguardian")
  relocate("com.google.gson", "gg.mineads.monitor.shadow.gson")
  relocate("com.google.protobuf", "gg.mineads.monitor.shadow.protobuf")
  relocate("com.google.errorprone", "gg.mineads.monitor.shadow.errorprone")
  relocate("com.tcoded.folialib", "gg.mineads.monitor.shadow.folialib")
  relocate("io.leangen.geantyref", "gg.mineads.monitor.shadow.geantyref")
  relocate("org.incendo", "gg.mineads.monitor.shadow.incendo")
  relocate("de.exlll.configlib", "gg.mineads.monitor.shadow.configlib")
}

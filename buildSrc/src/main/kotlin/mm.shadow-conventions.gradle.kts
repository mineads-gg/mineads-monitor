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
  }

  build {
    dependsOn(shadowJar)
  }
}

fun ShadowJar.configureRelocations() {
  relocate("io.papermc.lib", "gg.mineads.monitor.shadow.paperlib")
  relocate("org.bstats", "gg.mineads.monitor.shadow.bstats")
  relocate("gg.mineads.pistonutils", "gg.mineads.monitor.shadow.pistonutils")
  relocate("net.skinsrestorer.axiom", "gg.mineads.monitor.shadow.axiom")
  relocate("org.yaml.snakeyaml", "gg.mineads.monitor.shadow.snakeyaml")
  relocate("org.intellij.lang.annotations", "gg.mineads.monitor.shadow.annotations.intellij")
  relocate("org.jetbrains.annotations", "gg.mineads.monitor.shadow.annotations.jetbrains")
  relocate("com.google.gson", "gg.mineads.monitor.shadow.gson")
  relocate("com.google.errorprone", "gg.mineads.monitor.shadow.errorprone")
  relocate("net.lenni0451.mcstructs", "gg.mineads.monitor.shadow.mcstructs")
  relocate("com.tcoded.folialib", "gg.mineads.monitor.shadow.folialib")
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "MineAdsMonitor"

setOf(
    "build-data",
    "api",
    "shared",
    "bukkit",
    "bungee",
    "velocity"
).forEach { setupPMSubproject(it) }

fun setupPMSubproject(name: String) {
    setupSubproject("mineadsmonitor-$name") {
        projectDir = file(name)
    }
}

inline fun setupSubproject(name: String, block: ProjectDescriptor.() -> Unit) {
    include(name)
    project(":$name").apply(block)
}

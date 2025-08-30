import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("mm.shadow-conventions")
}

dependencies {
    implementation("net.kyori:adventure-api:4.24.0")
    implementation("net.kyori:adventure-text-serializer-legacy:4.24.0")
    implementation("net.kyori:adventure-text-serializer-gson:4.24.0")
    implementation("net.kyori:adventure-text-minimessage:4.24.0")
}

tasks.named<ShadowJar>("shadowJar").get().apply {
    relocate("net.kyori", "gg.mineads.monitor.shadow.kyori")
}

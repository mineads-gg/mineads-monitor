plugins {
    id("mm.java-conventions")
    id("mm.shadow-conventions")
}

dependencies {
    api("net.skinsrestorer:axiom:1.1.2-SNAPSHOT")
    api("net.lenni0451.mcstructs:text:3.1.0")
    implementation(project(":mineadsmonitor-api", "shadow"))

    compileOnly("com.github.LeonMangler:PremiumVanishAPI:2.9.18-2")

    compileOnly("net.luckperms:api:5.5")
}

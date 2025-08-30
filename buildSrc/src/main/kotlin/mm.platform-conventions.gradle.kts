import xyz.wagyourtail.jvmdg.gradle.task.DowngradeJar
import xyz.wagyourtail.jvmdg.gradle.task.ShadeJar

plugins {
    `java-library`
    id("xyz.wagyourtail.jvmdowngrader")
    id("mm.shadow-conventions")
}

val downgradePlatformBase = tasks.register<DowngradeJar>("downgradePlatformBase") {
    dependsOn(tasks.shadowJar)

    inputFile = tasks.shadowJar.get().archiveFile
    downgradeTo = JavaVersion.VERSION_1_8
    archiveClassifier.set("downgraded-base")
}

val downgradePlatformShadow = tasks.register<ShadeJar>("downgradePlatformShadow") {
    dependsOn(downgradePlatformBase)

    inputFile = downgradePlatformBase.get().archiveFile
    downgradeTo = JavaVersion.VERSION_1_8
    archiveFileName.set(
        "MineAdsMonitor-${
            project.name.substringAfter("mineadsmonitor-").replaceFirstChar(Char::titlecase)
        }-${project.version}.jar"
    )

    destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs"))
    shadePath = { _ -> "gg/mineads/monitor/shadow/jvmdowngrader" }
}

tasks {
    val downgradedTest by tasks.registering(Test::class) {
        group = "verification"
        useJUnitPlatform()
        dependsOn(downgradePlatformShadow)
        classpath = downgradePlatformShadow.get().outputs.files + sourceSets.test.get().output + sourceSets.test.get().runtimeClasspath - sourceSets.main.get().output
    }
    check {
        dependsOn(downgradedTest)
    }
}

configurations.create("downgraded")

artifacts {
    add("downgraded", downgradePlatformBase.get().archiveFile) {
        builtBy(downgradePlatformBase)
    }
}

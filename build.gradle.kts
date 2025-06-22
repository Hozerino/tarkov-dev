import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    kotlin("plugin.serialization") version "2.1.21"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("org.java-websocket:Java-WebSocket:1.5.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    implementation("org.jetbrains.skiko:skiko-awt-runtime-windows-x64:0.7.70")


}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "tarkov-compose"
            packageVersion = "1.0.0"
        }
    }
}

// Create a fat (uber) jar with dependencies and resources bundled
tasks.register<Jar>("fatJar") {
    group = "build"
    description = "Create a fat jar including all dependencies and resources"

    manifest {
        attributes["Main-Class"] = "MainKt"  // Your main class here
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    val runtimeClasspath = configurations.runtimeClasspath.get()
    from(files(runtimeClasspath.map { if (it.isDirectory) it else zipTree(it) }))

    // Include compiled Kotlin classes
    from(sourceSets.main.get().output)

    // Include resources (automatically from src/main/resources)
    from(sourceSets.main.get().resources)

    archiveBaseName.set("tarkov-compose-all")
    archiveVersion.set(version.toString())
    archiveClassifier.set("")

    // Optional: you can specify output directory
    destinationDirectory.set(layout.buildDirectory.dir("libs"))
}
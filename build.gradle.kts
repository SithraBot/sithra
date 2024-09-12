val ktor_version: String by project
val logback_version: String by project

plugins {
    kotlin("jvm") version "2.0.10"
    kotlin("plugin.serialization") version "2.0.20"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
}

application {
    mainClass = "top.ninnana.MainKt"
}

group = "top.ninnana"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-client-cio-jvm:2.3.12")
    testImplementation(kotlin("test"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.2")
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-websockets:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.klogging:slf4j-klogging:0.7.2")
    implementation("com.aallam.openai:openai-client:3.8.2")
    implementation("com.charleskorn.kaml:kaml:0.61.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes(mapOf("Main-Class" to "top.ninnana.MainKt"))
    }
}

kotlin {
    jvmToolchain(17)
}
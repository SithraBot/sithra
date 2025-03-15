import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*

val ktor_version: String by project
val project_version: String by project

plugins {
    kotlin("jvm") version "2.1.10"
    kotlin("plugin.serialization") version "2.1.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
}

val secrets: Properties by lazy {
    project.rootProject.file("secrets.properties").reader().use {
        Properties().apply { load(it) }
    }
}

application {
    mainClass = "org.sithra.sithrabot.MainKt"
}

group = "org.sithra.sithrabot"
version = project_version

repositories {
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/SithraBot/synthetic")
        credentials {
            username = System.getenv("GITHUB_USERNAME") ?: secrets.getProperty("github.username")!!
            password = System.getenv("GITHUB_TOKEN") ?: secrets.getProperty("github.token")!!
        }
    }
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation("ch.qos.logback:logback-classic:1.5.17")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.5")
    implementation("org.sithra.synthetic:core:0.1.0-alpha")
    implementation("org.sithra.synthetic:openai:0.1.0-alpha")
    implementation("io.ktor:ktor-client-cio-jvm:$ktor_version")
    implementation("com.charleskorn.kaml:kaml:0.72.0")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes("Main-Class" to "org.sithra.sithrabot.MainKt")
    }
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
    }
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        // freeCompilerArgs.add("-parameters")
    }
}
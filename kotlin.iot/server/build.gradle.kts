val ktor_version: String by rootProject
val kotlin_version: String by rootProject
val logback_version: String by rootProject

plugins {
    id("io.ktor.plugin") version "2.3.1"
    application
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.8.22"
}

ktor {
    fatJar {
        archiveFileName.set("iotSystem.jar")
    }
}

application {
    mainClass.set("ru.zhuravlev.yuri.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-freemarker:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")

    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":controller"))
    implementation(project(":adapter:mqtt"))
}
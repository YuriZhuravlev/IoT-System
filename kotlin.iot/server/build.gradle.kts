val ktor_version: String by rootProject
val kotlin_version: String by rootProject
val logback_version: String by rootProject

plugins {
    application
    kotlin("jvm")
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
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
}
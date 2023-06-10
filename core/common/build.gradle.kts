plugins {
    kotlin("jvm") version "1.8.22"
}

group = "ru.zhuravlev.yuri.iot.core"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(11)
}
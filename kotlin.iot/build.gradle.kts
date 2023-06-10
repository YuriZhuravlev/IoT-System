plugins {
    base
    kotlin("jvm") version "1.8.22"
}

allprojects {
    group = "ru.zhuravlev.yuri"
    version = "1.0"

    apply(plugin = "kotlin")

    repositories {
        mavenLocal()
        maven { url = uri("https://plugins.gradle.org/m2/") }
    }
}

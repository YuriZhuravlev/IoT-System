pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
    }
}

rootProject.name = "iot-system"

include(
        "server",
        "core:common",
        "core:model",
        "adapter:mqtt",
        "controller",
        "device"
)
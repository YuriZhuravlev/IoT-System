pluginManagement {
    repositories {
        mavenCentral()
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
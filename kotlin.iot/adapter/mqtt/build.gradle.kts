plugins {
    kotlin("jvm")
}

dependencies {
    implementation("com.ditchoom:mqtt-client:1.1.4")
    implementation("com.ditchoom:mqtt-5-models:1.1.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.4")

    implementation(project(":core:common"))
    implementation(project(":core:model"))
}
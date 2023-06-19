package ru.zhuravlev.yuri.emulator

import kotlinx.coroutines.delay

suspend fun main() {
    DeviceEmulator(emulateDevice1 = true, emulateDevice2 = true)
    while (true) {
        delay(6000)
    }
}
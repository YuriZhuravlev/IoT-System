package ru.zhuravlev.yuri.emulator.producers

import kotlinx.coroutines.CoroutineScope

class TemperatureProducer(coroutineScope: CoroutineScope) : DefaultProducer(
        coroutineScope,
        5,
        25,
        100,
        1000,
        { it + 1 }
)
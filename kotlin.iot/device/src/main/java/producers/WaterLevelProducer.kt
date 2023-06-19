package ru.zhuravlev.yuri.emulator.producers

import kotlinx.coroutines.CoroutineScope

class WaterLevelProducer(coroutineScope: CoroutineScope) : DefaultProducer(
        coroutineScope,
        0,
        2,
        100,
        1400,
        {
            it + 1 + it.div(20)
        }
)
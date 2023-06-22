package ru.zhuravlev.yuri.emulator.producers

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

abstract class DefaultProducer(
        coroutineScope: CoroutineScope,
        minValue: Int,
        maxStartValue: Int,
        maxValue: Int,
        delayValue: Long,
        changeFun: (Int) -> Int
) {
    private val random = Random(Random.nextInt())
    private val _flow = MutableStateFlow(random.nextInt(minValue, maxStartValue))
    val flow = _flow.asStateFlow()

    init {
        coroutineScope.launch {
            while (_flow.value < maxValue) {
                if (random.nextBoolean()) {
                    _flow.emit(minOf(changeFun(_flow.value), maxValue))
                }
                delay(delayValue)
            }
        }
    }
}
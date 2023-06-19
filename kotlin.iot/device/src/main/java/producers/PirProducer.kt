package ru.zhuravlev.yuri.emulator.producers

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class PirProducer(coroutineScope: CoroutineScope) {
    private val random = Random(Random.nextInt())
    private val _flow = MutableStateFlow(false)
    val flow = _flow.asStateFlow()

    init {
        coroutineScope.launch {
            while (true) {
                _flow.emit(random.nextBoolean())
                delay(20000)
            }
        }
    }
}
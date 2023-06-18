package ru.zhuravlev.yuri.controller

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.zhuravlev.yuri.core.NetworkWorker
import ru.zhuravlev.yuri.core.PushSender
import ru.zhuravlev.yuri.core.model.*

class Controller(
        private val networkWorker: NetworkWorker,
        private val pushSender: PushSender
) {
    private val context = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _state = MutableStateFlow(SystemState.INIT)
    private val _error = MutableSharedFlow<Throwable>()
    val state = _state.asStateFlow()
    val error = _error.asSharedFlow()

    private val messageHandler = object : NetworkWorker.MessageHandler {
        override fun onTemperature(temperature: Temperature) {
            context.launch {
                _state.emit(_state.value.copy(temperature = temperature))
            }
        }

        override fun onPassiveInfraredSensor(pir: PassiveInfraredSensor) {
            context.launch {
                _state.emit(_state.value.copy(pir = pir))
            }
        }

        override fun onWaterLevel(waterLevel: WaterLevel) {
            context.launch {
                _state.emit(_state.value.copy(waterLevel = waterLevel))
            }
        }

        override fun onError(error: Throwable) {
            context.launch {
                _error.emit(error)
            }
        }
    }

    init {
        networkWorker.subscribe(onMessage = messageHandler)
        startProducing()
    }

    private fun startProducing() {
        context.launch(CoroutineExceptionHandler { _, throwable ->
            context.launch {
                _error.emit(throwable)
            }
        }) {
            state.collect { systemState ->
                if (systemState == SystemState.INIT || systemState.configurationTemperature.isEmpty()) {
                    // Skip state
                } else {
                    when {
                        systemState.configurationTemperature.lastIndex > systemState.temperatureIndex &&
                                systemState.configurationTemperature.temperatures[systemState.temperatureIndex + 1] >= systemState.temperature -> {
                            val newState = systemState.copy(temperatureIndex = systemState.temperatureIndex)
                            _state.emit(newState)

                            if (systemState.pir.activeMoving) {
                                networkWorker.publish(UserSignal.BLEEPER)
                            }
                            pushSender.sendPush(UserSignal.Push(newState))
                        }

                        systemState.waterLevel.value > ATTENTION_WATER_LEVEL -> {
                            networkWorker.publish(UserSignal.BLEEPER)
                            pushSender.sendPush(UserSignal.Push(systemState))
                        }

                        systemState.waterLevel.value > NOTIFY_WATER_LEVEL -> pushSender.sendPush(UserSignal.Push(systemState))
                    }
                }
            }
        }
    }

    fun setConfigTemperature(temperature: ConfigurationTemperature) {
        context.launch {
            networkWorker.publish(temperature)
            _state.emit(_state.value.copy(configurationTemperature = temperature))
        }
    }

    companion object {
        private const val NOTIFY_WATER_LEVEL = 90
        private const val ATTENTION_WATER_LEVEL = 95
    }
}
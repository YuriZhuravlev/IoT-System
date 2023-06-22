package ru.zhuravlev.yuri.adapter.push

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.zhuravlev.yuri.core.PushSender
import ru.zhuravlev.yuri.core.model.Consts.NOTIFY_WATER_LEVEL
import ru.zhuravlev.yuri.core.model.UserSignal

class SimplePushSender : PushSender {
    private val context = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _push = MutableSharedFlow<UserSignal.Push>()
    val push = _push.map {
        val systemState = it.state
        when {
            (systemState.temperatureIndex >= systemState.configurationTemperature.lastIndex) -> {
                "Reaching the maximum temperature! Turn off the heat"
            }

            (systemState.waterLevel.value >= NOTIFY_WATER_LEVEL) -> {
                "Attention! Water level is ${systemState.waterLevel.value}%"
            }

            else -> "${systemState.temperature.value}℃, ${systemState.waterLevel.value}%"
        }
    }

    init {
        context.launch {
            push.collect { message ->
                println("Sending push: $message")
            }
        }
    }

    override fun sendPush(push: UserSignal.Push) {
        context.launch {
            _push.emit(push)
        }
    }
}
package ru.zhuravlev.yuri.core

import ru.zhuravlev.yuri.core.model.*

interface NetworkWorker {
    fun subscribe(onMessage: MessageHandler)
    fun unsubscribe()
    fun publish(configurationTemperature: ConfigurationTemperature)
    fun publish(signal: UserSignal)

    interface MessageHandler {
        fun onTemperature(temperature: Temperature)
        fun onPassiveInfraredSensor(pir: PassiveInfraredSensor)
        fun onWaterLevel(waterLevel: WaterLevel)
        fun onError(error: Throwable)
    }

}
package ru.zhuravlev.yuri.core.model

data class SystemState(
        val temperature: Temperature,
        val waterLevel: WaterLevel,
        val pir: PassiveInfraredSensor,
        val configurationTemperature: ConfigurationTemperature
)

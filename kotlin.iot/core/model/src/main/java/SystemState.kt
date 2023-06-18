package ru.zhuravlev.yuri.core.model

data class SystemState(
        val temperature: Temperature,
        val waterLevel: WaterLevel,
        val pir: PassiveInfraredSensor,
        val configurationTemperature: ConfigurationTemperature,
        val temperatureIndex: Int
) {
    companion object {
        const val INIT_VALUE = -1

        val INIT = SystemState(
                Temperature(INIT_VALUE),
                WaterLevel(INIT_VALUE),
                PassiveInfraredSensor(false),
                ConfigurationTemperature(listOf()),
                INIT_VALUE
        )
    }
}

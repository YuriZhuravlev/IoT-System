package ru.zhuravlev.yuri.model

import kotlinx.serialization.Serializable
import ru.zhuravlev.yuri.core.model.SystemState

@Serializable
data class SystemData(
        val temperature: Int? = null,
        val waterLevel: Int? = null,
        val pirIsActive: Boolean? = null,
        val nextTemperature: Int? = null,
        val error: String? = null
) {
    companion object {
        fun state(state: SystemState) = SystemData(
                temperature = if (state.temperature.value in 0..100) state.temperature.value else null,
                waterLevel = if (state.waterLevel.value in 0..100) state.waterLevel.value else null,
                pirIsActive = state.pir.activeMoving,
                nextTemperature = state.configurationTemperature.temperatures.getOrNull(state.temperatureIndex + 1)?.value
        )

        fun error(message: String?) = SystemData(error = message ?: "Unknown error")
    }
}

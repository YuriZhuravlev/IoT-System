package ru.zhuravlev.yuri.core.model

data class ConfigurationTemperature(
        val temperatures: List<Temperature>
) {
    fun isEmpty() = temperatures.isEmpty()

    val lastIndex get() = temperatures.lastIndex
}

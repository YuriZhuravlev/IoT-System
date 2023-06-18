package ru.zhuravlev.yuri.core.model

data class Temperature(val value: Int) {
    operator fun compareTo(temperature: Temperature): Int =
            value.compareTo(temperature.value)
}

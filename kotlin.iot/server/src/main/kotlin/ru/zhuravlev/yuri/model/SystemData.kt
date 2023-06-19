package ru.zhuravlev.yuri.model

import kotlinx.serialization.Serializable

@Serializable
data class SystemData(
        val temperature: Int? = null,
        val waterLevel: Int? = null,
        val pirIsActive: Boolean? = null,
        val nextTemperature: Int? = null,
        val error: String? = null
)

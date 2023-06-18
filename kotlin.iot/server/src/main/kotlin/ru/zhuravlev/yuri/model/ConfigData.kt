package ru.zhuravlev.yuri.model

import kotlinx.serialization.Serializable

@Serializable
data class ConfigData(
        val temperature: List<Int>
)
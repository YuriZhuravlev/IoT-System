package ru.zhuravlev.yuri.core

import java.io.File
import java.util.*

object BuildConfig {
    private val properties by lazy {
        Properties().apply { load(File(PROPERTIES_PATH).reader()) }
    }

    fun getString(name: String): String? = kotlin.runCatching {
        properties.getProperty(name) as? String
    }.getOrNull()

    private const val PROPERTIES_PATH = "kotlin.iot/local.properties"
}
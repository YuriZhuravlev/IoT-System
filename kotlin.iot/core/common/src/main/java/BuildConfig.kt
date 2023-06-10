package ru.zhuravlev.yuri.core

object BuildConfig {
    private val properties by lazy {
        System.getProperties()
    }

    fun getString(name: String): String? = kotlin.runCatching {
        properties.getProperty(name) as? String
    }.getOrNull()
}
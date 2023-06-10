package ru.zhuravlev.yuri.core.model

import kotlin.time.Duration


class Broker(
        val id: Int,
        val params: Collection<ConnectionParams>
)

data class ConnectionParams(
        val host: String,
        val port: Int,
        val connectionTimeout: Duration
)

package ru.zhuravlev.yuri.core.model

abstract class BrokerClient {
    abstract suspend fun publish()
    abstract suspend fun subscribe()
    abstract suspend fun unsubscribe()
}
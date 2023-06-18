package ru.zhuravlev.yuri.core.model

sealed class UserSignal {
    class Push(val state: SystemState) : UserSignal()
    object BLEEPER : UserSignal()
}
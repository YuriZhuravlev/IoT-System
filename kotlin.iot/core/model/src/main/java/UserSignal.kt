package ru.zhuravlev.yuri.core.model

sealed class UserSignal {
    object PUSH : UserSignal()
    object BLEEPER : UserSignal()
}
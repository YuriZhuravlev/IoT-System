package ru.zhuravlev.yuri.core

import ru.zhuravlev.yuri.core.model.UserSignal

interface PushSender {
    fun sendPush(push: UserSignal.Push)
}
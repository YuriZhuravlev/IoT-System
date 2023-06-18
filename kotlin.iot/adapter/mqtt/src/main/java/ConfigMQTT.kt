package ru.zhuravlev.yuri.adapter.mqtt

import com.ditchoom.mqtt.controlpacket.Topic
import com.ditchoom.mqtt5.controlpacket.Subscription

object ConfigMQTT {
    object Publisher {
        const val CONFIGURATION_TEMPERATURE = "config/configuration_temperature"
        const val BLEEPER = "action/bleeper"
    }

    object Subscriptions {
        private const val TEMPERATURE = "state/temperature"
        private const val WATER_LEVEL = "state/water_level"
        private const val PIR = "state/pir"

        val temperature by lazy { Subscription(Topic.fromOrThrow(TEMPERATURE, Topic.Type.Name)) }
        val waterLevel by lazy { Subscription(Topic.fromOrThrow(WATER_LEVEL, Topic.Type.Name)) }
        val pir by lazy { Subscription(Topic.fromOrThrow(PIR, Topic.Type.Name)) }
    }
}
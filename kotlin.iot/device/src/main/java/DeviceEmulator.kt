package ru.zhuravlev.yuri.emulator

import com.ditchoom.buffer.JvmBuffer
import com.ditchoom.buffer.ReadBuffer
import com.ditchoom.mqtt.client.MqttClient
import com.ditchoom.mqtt.client.MqttService
import com.ditchoom.mqtt.connection.MqttConnectionOptions
import com.ditchoom.mqtt.controlpacket.ControlPacket.Companion.readVariableByteInteger
import com.ditchoom.mqtt.controlpacket.IPublishMessage
import com.ditchoom.mqtt.controlpacket.Topic
import com.ditchoom.mqtt5.controlpacket.ConnectionRequest
import com.ditchoom.mqtt5.controlpacket.Subscription
import kotlinx.coroutines.*
import ru.zhuravlev.yuri.adapter.mqtt.ConfigMQTT
import ru.zhuravlev.yuri.core.BuildConfig
import ru.zhuravlev.yuri.emulator.producers.PirProducer
import ru.zhuravlev.yuri.emulator.producers.TemperatureProducer
import ru.zhuravlev.yuri.emulator.producers.WaterLevelProducer
import java.nio.ByteBuffer
import java.util.*

class DeviceEmulator(emulateDevice1: Boolean, emulateDevice2: Boolean) {
    private val context = CoroutineScope(SupervisorJob() + CoroutineExceptionHandler { coroutineContext, throwable ->
        println(throwable)
    })
    private var client1: MqttClient? = null
    private var client2: MqttClient? = null

    init {
        context.launch {
            val job1 = context.launch {
                if (emulateDevice1) {
                    client1 = initClient(
                            BuildConfig.getString(DEVICE1_CLIENT_ID)!!,
                            BuildConfig.getString(DEVICE1_USERNAME)!!,
                            BuildConfig.getString(DEVICE1_PASSWORD)!!
                    )
                }
            }
            val job2 = context.launch {
                if (emulateDevice2) {
                    client2 = initClient(
                            BuildConfig.getString(DEVICE2_CLIENT_ID)!!,
                            BuildConfig.getString(DEVICE2_USERNAME)!!,
                            BuildConfig.getString(DEVICE2_PASSWORD)!!
                    )
                }
            }
            listOf(job1, job2).joinAll()
            if (emulateDevice1) {
                context.launch {
                    TemperatureProducer(this).flow.collect {
                        println("${Date()}: temperature=$it")
                        client1?.publish(TEMPERATURE, payload = write(it.toString()))
                    }
                }
                context.launch {
                    WaterLevelProducer(this).flow.collect {
                        println("${Date()}: water_level=$it")
                        client1?.publish(WATER_LEVEL, payload = write(it.toString()))
                    }
                }
            }
            if (emulateDevice2) {
                context.launch {
                    PirProducer(this).flow.collect {
                        println("${Date()}: pir=$it")
                        client2?.publish(PIR, payload = write((if (it) 1 else 0).toString()))
                    }
                }
                context.launch(SupervisorJob()) {
                    val config = Subscription(
                            Topic.fromOrThrow(
                                    ConfigMQTT.Publisher.CONFIGURATION_TEMPERATURE,
                                    Topic.Type.Name
                            )
                    )
                    val bleeper = Subscription(Topic.fromOrThrow(ConfigMQTT.Publisher.BLEEPER, Topic.Type.Name))
                    client2?.subscribe(setOf(config, bleeper))?.subscriptions?.forEach { (key, value) ->
                        launch {
                            when (key) {
                                config -> {
                                    value.collect {
                                        println("${Date()}: config=[${it.payload?.readUtf8Line()}]")
                                    }
                                }

                                bleeper -> {
                                    value.collect {
                                        println("${Date()}: bleeper=${it.payload?.readUtf8Line()}")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun readOrNull(message: IPublishMessage): Int {
        return kotlin.runCatching { message.payload!!.readVariableByteInteger() }.getOrDefault(0)
    }

    private suspend fun initClient(clientId: String, username: String, password: String): MqttClient {
        val service = MqttService.buildNewService(ipcEnabled = true, androidContextOrAbstractWorker = null)

        val socketEndPoint = MqttConnectionOptions.SocketConnection(host, port)
        val connections = listOf(socketEndPoint)
        val connectionRequest = ConnectionRequest(clientId = clientId, userName = username, password = password)
        return service.addBrokerAndStartClient(connections, connectionRequest)
    }

    private fun write(value: String): ReadBuffer {
        return JvmBuffer(ByteBuffer.wrap(value.toByteArray()))
    }

    companion object {
        private const val host = "dev.rightech.io"
        private const val port = 1883 // 8883 for TLS

        private const val DEVICE1_CLIENT_ID = "device1ClientID"
        private const val DEVICE1_USERNAME = "device1Username"
        private const val DEVICE1_PASSWORD = "device1Password"
        private const val DEVICE2_CLIENT_ID = "device2ClientID"
        private const val DEVICE2_USERNAME = "device2Username"
        private const val DEVICE2_PASSWORD = "device2Password"

        private const val TEMPERATURE = "state/temperature"
        private const val WATER_LEVEL = "state/water_level"
        private const val PIR = "state/pir"
    }
}
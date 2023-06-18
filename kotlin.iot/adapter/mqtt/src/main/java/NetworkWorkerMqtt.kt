package ru.zhuravlev.yuri.adapter.mqtt

import com.ditchoom.buffer.AllocationZone
import com.ditchoom.buffer.PlatformBuffer
import com.ditchoom.buffer.allocate
import com.ditchoom.mqtt.client.MqttService
import com.ditchoom.mqtt.connection.MqttConnectionOptions
import com.ditchoom.mqtt.controlpacket.QualityOfService
import com.ditchoom.mqtt5.controlpacket.ConnectionRequest
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ru.zhuravlev.yuri.core.BuildConfig

class NetworkWorkerMqtt {
    private val context = CoroutineScope(SupervisorJob())

    fun subscribe(onMessage: () -> Unit, onError: (Throwable) -> Unit) {
        context.launch(CoroutineExceptionHandler { _, throwable -> onError(throwable) }) {
            val service = MqttService.buildNewService(ipcEnabled = true, androidContextOrAbstractWorker = null)

            val socketEndPoint = MqttConnectionOptions.SocketConnection(host, port)
            val connections = listOf(socketEndPoint)
            val clientId = BuildConfig.getString(SERVER_CLIENT_ID)
            val username = BuildConfig.getString(SERVER_USERNAME)
            val password = BuildConfig.getString(SERVER_PASSWORD)
            if (clientId == null || username == null || password == null) {
                onError(Exception("Properties for server config not found"))
            } else {
                val connectionRequest = ConnectionRequest(clientId = clientId, userName = username, password = password)
                val client = service.addBrokerAndStartClient(connections, connectionRequest)

                val subscribeOperation = client.subscribe(
                        setOf(
                                ConfigMQTT.Subscriptions.pit,
                                ConfigMQTT.Subscriptions.temperature,
                                ConfigMQTT.Subscriptions.waterLevel
                        )
                )

                subscribeOperation.subscriptions.map {
                    it.value
                }
            }
        }
    }

    suspend fun connect() {
        val service = MqttService.buildNewService(ipcEnabled = true, androidContextOrAbstractWorker = null, inMemory = false)

        val socketEndPoint = MqttConnectionOptions.SocketConnection(host, port)
//        val wsEndpoint = MqttConnectionOptions.WebSocketConnectionOptions(host, port) maybe "ws://ws.dev.rightech.io"
        val connections = listOf(socketEndPoint)
        val clientId = BuildConfig.getString(SERVER_CLIENT_ID)
        val username = BuildConfig.getString(SERVER_USERNAME)
        val password = BuildConfig.getString(SERVER_PASSWORD)
        if (clientId == null || username == null || password == null) {
            throw Exception("Properties for server config not found")
        }
        val connectionRequest = ConnectionRequest(clientId = clientId, userName = username, password = password)

        val client = service.addBrokerAndStartClient(connections, connectionRequest)

        val subscribeOperation = client.subscribe("test/+", QualityOfService.AT_LEAST_ONCE) // TODO topicFilter


        // optional, await for suback before proceeding
        val subAck = subscribeOperation.subAck.await() // хз
        // optional, subscribe to incoming publish on the topic
        val topicFlow = subscribeOperation.subscriptions.values.first()

        val payloadBuffer = PlatformBuffer.allocate(4, AllocationZone.SharedMemory)
        //Cast to JvmBuffer/JsBuffer/DataBuffer and retrieve underlying ByteBuffer/ArrayBuffer/NSData to modify contents
        payloadBuffer.writeString("taco") // just write utf8 string data for now
        val pubOperation = client.publish("test/123", QualityOfService.EXACTLY_ONCE, payloadBuffer)
        pubOperation.awaitAll() // suspend until

        val unsubscribeOperation = client.unsubscribe("test/+")
        unsubscribeOperation.unsubAck.await()

        client.shutdown()
    }

    companion object {
        private const val host = "dev.rightech.io"
        private const val port = 1883 // 8883 for TLS

        private const val SERVER_CLIENT_ID = "serverClientID"
        private const val SERVER_USERNAME = "serverUsername"
        private const val SERVER_PASSWORD = "serverPassword"
    }
}
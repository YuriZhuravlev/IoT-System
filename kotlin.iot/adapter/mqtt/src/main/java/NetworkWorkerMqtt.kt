package ru.zhuravlev.yuri.adapter.mqtt

import com.ditchoom.mqtt.client.MqttClient
import com.ditchoom.mqtt.client.MqttService
import com.ditchoom.mqtt.connection.MqttConnectionOptions
import com.ditchoom.mqtt.controlpacket.IPublishMessage
import com.ditchoom.mqtt5.controlpacket.ConnectionRequest
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.zhuravlev.yuri.core.BuildConfig
import ru.zhuravlev.yuri.core.NetworkWorker
import ru.zhuravlev.yuri.core.model.ConfigurationTemperature
import ru.zhuravlev.yuri.core.model.UserSignal

class NetworkWorkerMqtt : NetworkWorker {
    private val context = CoroutineScope(
            SupervisorJob() +
                    Dispatchers.IO +
                    CoroutineExceptionHandler { _, throwable -> handler?.onError(throwable) }
    )
    private val mapper = Mapper()
    private val subscriptions by lazy {
        setOf(
                ConfigMQTT.Subscriptions.pit,
                ConfigMQTT.Subscriptions.temperature,
                ConfigMQTT.Subscriptions.waterLevel
        )
    }

    private var _client: MqttClient? = null
    private var handler: NetworkWorker.MessageHandler? = null

    private val mutex = Mutex()

    override fun subscribe(onMessage: NetworkWorker.MessageHandler) {
        context.launch {
            val subscribeOperation = mutex.withLock {
                val client = initClient()
                client.subscribe(subscriptions)
            }

            subscribeOperation.subscriptions.forEach { (key, flow) ->
                when (key) {
                    ConfigMQTT.Subscriptions.pit -> {
                        launch {
                            subscribeAndParse(flow, onMessage::onPassiveInfraredSensor)
                        }
                    }

                    ConfigMQTT.Subscriptions.temperature -> {
                        launch {
                            subscribeAndParse(flow, onMessage::onTemperature)
                        }
                    }

                    ConfigMQTT.Subscriptions.waterLevel -> {
                        launch {
                            subscribeAndParse(flow, onMessage::onWaterLevel)
                        }
                    }
                }
            }
        }
    }

    override fun unsubscribe() {
        context.launch {
            mutex.withLock {
                _client?.unsubscribe(subscriptions.map { it.topicFilter }.toSet())
                _client?.shutdown()
                _client = null
            }
        }
    }

    override fun publish(configurationTemperature: ConfigurationTemperature) {
        context.launch {
            mutex.withLock {
                val client = _client ?: initClient()
                client.publish(
                        ConfigMQTT.Publisher.CONFIGURATION_TEMPERATURE,
                        payload = mapper.write(configurationTemperature)
                )
            }
        }
    }

    override fun publish(signal: UserSignal) {
        if (signal == UserSignal.BLEEPER) {
            context.launch {
                mutex.withLock {
                    val client = _client ?: initClient()
                    client.publish(ConfigMQTT.Publisher.BLEEPER)
                }
            }
        }
    }

    private suspend fun initClient(): MqttClient {
        val service = MqttService.buildNewService(ipcEnabled = true, androidContextOrAbstractWorker = null)

        val socketEndPoint = MqttConnectionOptions.SocketConnection(host, port)
        val connections = listOf(socketEndPoint)
        val clientId = BuildConfig.getString(SERVER_CLIENT_ID)
        val username = BuildConfig.getString(SERVER_USERNAME)
        val password = BuildConfig.getString(SERVER_PASSWORD)
        if (clientId == null || username == null || password == null)
            throw Exception("Properties for server config not found")

        val connectionRequest = ConnectionRequest(clientId = clientId, userName = username, password = password)
        val client = service.addBrokerAndStartClient(connections, connectionRequest)
        _client = client
        return client
    }

    private suspend inline fun <reified T> subscribeAndParse(
            flow: Flow<IPublishMessage>,
            crossinline send: (T) -> Unit
    ) {
        flow.collect { message ->
            mapper.map<T>(message.payload)?.run(send)
        }
    }

    companion object {
        private const val host = "dev.rightech.io"
        private const val port = 1883 // 8883 for TLS

        private const val SERVER_CLIENT_ID = "serverClientID"
        private const val SERVER_USERNAME = "serverUsername"
        private const val SERVER_PASSWORD = "serverPassword"
    }
}
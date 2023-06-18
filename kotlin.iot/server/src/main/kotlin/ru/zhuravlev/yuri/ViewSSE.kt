package ru.zhuravlev.yuri

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.produceIn
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.zhuravlev.yuri.adapter.mqtt.NetworkWorkerMqtt
import ru.zhuravlev.yuri.controller.Controller
import ru.zhuravlev.yuri.core.PushSender
import ru.zhuravlev.yuri.core.model.ConfigurationTemperature
import ru.zhuravlev.yuri.core.model.Temperature
import ru.zhuravlev.yuri.core.model.UserSignal
import ru.zhuravlev.yuri.model.ConfigData
import ru.zhuravlev.yuri.model.EventType
import ru.zhuravlev.yuri.model.SystemData

object ViewSSE {
    private val pushSender = object : PushSender {
        override fun sendPush(push: UserSignal.Push) {
            println("Sending push: ${push.state}")
            // TODO("add later")
        }
    }

    private val controller = Controller(NetworkWorkerMqtt(), pushSender)

    suspend fun view(call: ApplicationCall) {
        val state = controller.state.map { state ->
            val systemData = SystemData(
                    state.temperature.value,
                    state.waterLevel.value,
                    state.configurationTemperature.temperatures.getOrNull(state.temperatureIndex + 1)?.value
            )
            SseEvent(data = Json.encodeToString(systemData), EventType.STATE_EVENT)
        }
        val error = controller.error.map {
            val systemData = SystemData(error = it.message)
            SseEvent(data = Json.encodeToString(systemData), EventType.ERROR_EVENT)
        }
        call.respondSse(merge(state, error).produceIn(CoroutineScope(SupervisorJob())))
    }

    suspend fun updateConfig(call: ApplicationCall) {
        try {
            val config = Json.decodeFromString<ConfigData>(call.receiveText())
            controller.setConfigTemperature(
                    ConfigurationTemperature(
                            config.temperature.map { value -> Temperature(value) }
                    )
            )
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, e.printStackTrace())
        }
    }

    fun getConfig() = controller.state.value.configurationTemperature

    data class SseEvent(val data: String, val event: String? = null, val id: String? = null)

    suspend fun ApplicationCall.respondSse(events: ReceiveChannel<SseEvent>) {
        response.cacheControl(CacheControl.NoCache(null))
        respondTextWriter(contentType = ContentType.Text.EventStream) {
            for (event in events) {
                if (event.id != null) {
                    write("id: ${event.id}\n")
                }
                if (event.event != null) {
                    write("event: ${event.event}\n")
                }
                for (dataLine in event.data.lines()) {
                    write("data: $dataLine\n")
                }
                write("\n")
                flush()
            }
        }
    }
}
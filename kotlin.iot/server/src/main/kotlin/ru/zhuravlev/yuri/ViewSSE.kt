package ru.zhuravlev.yuri

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.zhuravlev.yuri.adapter.mqtt.NetworkWorkerMqtt
import ru.zhuravlev.yuri.adapter.push.SimplePushSender
import ru.zhuravlev.yuri.controller.Controller
import ru.zhuravlev.yuri.core.model.ConfigurationTemperature
import ru.zhuravlev.yuri.core.model.Temperature
import ru.zhuravlev.yuri.model.ConfigData
import ru.zhuravlev.yuri.model.EventType
import ru.zhuravlev.yuri.model.SystemData

object ViewSSE {
    private val pushSender = SimplePushSender()

    private val controller = Controller(NetworkWorkerMqtt(), pushSender)

    suspend fun view(call: ApplicationCall) {
        val state = controller.state.map { state ->
            val systemData = SystemData.state(state)
            SseEvent(data = Json.encodeToString(systemData), EventType.STATE_EVENT)
        }
        val error = controller.error.map {
            val systemData = SystemData.error(it.message)
            SseEvent(data = Json.encodeToString(systemData), EventType.ERROR_EVENT)
        }
        call.respondSse(merge(error, state))
    }

    suspend fun updateConfig(call: ApplicationCall) {
        try {
            val config = Json.decodeFromString<ConfigData>(call.receiveText())
            if (config.temperature.find { element -> element !in 0..100 } == null && config.temperature.isNotEmpty()) {
                controller.setConfigTemperature(
                        ConfigurationTemperature(
                                config.temperature.map { value -> Temperature(value) }
                        )
                )
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.BadRequest, "Invalid temperatures [0, 100]")
            }
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, e.printStackTrace())
        }
    }

    fun getConfig() = controller.state.value.configurationTemperature

    data class SseEvent(val data: String, val event: String? = null, val id: String? = null)

    private suspend fun ApplicationCall.respondSse(events: Flow<SseEvent>) {
        response.cacheControl(CacheControl.NoCache(null))
        respondTextWriter(contentType = ContentType.Text.EventStream) {
            events.collect { event ->
                withContext(Dispatchers.IO) {
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

    suspend fun addPushSubscriber(call: ApplicationCall) {
        val events = pushSender.push.map { SseEvent(it, EventType.PUSH) }
        call.respondSse(events)
    }
}
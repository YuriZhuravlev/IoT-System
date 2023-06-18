package ru.zhuravlev.yuri.plugins

import io.ktor.server.application.*
import io.ktor.server.freemarker.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ru.zhuravlev.yuri.ViewSSE

fun Application.configureRouting() {
    routing {
        get("/") {
            val config = ViewSSE.getConfig().temperatures.joinToString(prefix = "(", postfix = ")")
            call.respond(FreeMarkerContent("index.html", mapOf("config" to config)))
        }
        get("/subscribe") {
            ViewSSE.view(call)
        }
        post("config") {
            ViewSSE.updateConfig(call)
        }
    }
}

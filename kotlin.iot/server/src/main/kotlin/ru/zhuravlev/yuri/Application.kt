package ru.zhuravlev.yuri

import freemarker.cache.ClassTemplateLoader
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.freemarker.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.statuspages.*
import ru.zhuravlev.yuri.plugins.configureRouting
import ru.zhuravlev.yuri.plugins.statusPageConfig


fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureRouting()
    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
    }
    install(StatusPages, ::statusPageConfig)
}

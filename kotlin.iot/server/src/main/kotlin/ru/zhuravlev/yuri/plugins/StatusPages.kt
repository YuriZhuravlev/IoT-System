package ru.zhuravlev.yuri.plugins

import io.ktor.http.*
import io.ktor.server.freemarker.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

private const val HEADER = "header"
private const val BODY = "body"
private const val TEMPLATE_STATUS = "status_page.html"

private const val PageNotFound = "404: Page Not Found"

fun statusPageConfig(statusPagesConfig: StatusPagesConfig) {
    statusPagesConfig.exception<Throwable> { call, cause ->
        val status = HttpStatusCode.InternalServerError
        call.respond(status, FreeMarkerContent(
                TEMPLATE_STATUS,
                mapOf(HEADER to status, BODY to "${status.value}: $cause")
        ))
    }

    statusPagesConfig.status(HttpStatusCode.NotFound) { call, status ->
        call.respond(status, FreeMarkerContent(
                TEMPLATE_STATUS,
                mapOf(HEADER to status.description, BODY to PageNotFound)
        ))
    }
}

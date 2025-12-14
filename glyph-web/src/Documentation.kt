package org.yttr.glyph.config

import io.ktor.server.mustache.MustacheContent
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

/**
 * Static documentation and redirects.
 */
fun Route.documentation(clientId: String) {
    get("/") {
        call.respond(MustacheContent(template = "home.hbs", model = null))
    }

    get("/privacy") {
        call.respond(MustacheContent(template = "privacy.hbs", model = null))
    }

    get("/config") {
        call.respondRedirect("/#config")
    }

    get("/skills") {
        call.respondRedirect("/#skills")
    }

    get("/tip") {
        call.respondRedirect("https://ko-fi.com/throudin/")
    }

    get("/sponsor") {
        call.respondRedirect("https://ko-fi.com/throudin")
    }

    get("/invite") {
        call.respondRedirect("https://discord.com/oauth2/authorize?client_id=$clientId&scope=bot&permissions=805593158")
    }

    get("/source") {
        call.respondRedirect("https://github.com/yttrian/glyph.kt/")
    }

    get("/server") {
        call.respondRedirect("https://discord.gg/Meet9mF")
    }
}

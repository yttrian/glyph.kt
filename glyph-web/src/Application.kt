package org.yttr.glyph.config

import com.github.mustachejava.DefaultMustacheFactory
import com.typesafe.config.ConfigFactory
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.mustache.Mustache
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.autohead.AutoHeadResponse
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.plugins.forwardedheaders.XForwardedHeaders
import io.ktor.server.request.path
import io.ktor.server.routing.IgnoreTrailingSlash
import io.ktor.server.routing.routing
import io.ktor.server.webjars.Webjars
import org.slf4j.event.Level

/**
 * The entry point of the Ktor webs server
 */
fun main(args: Array<String>): Unit = EngineMain.main(args)

/**
 * The main module to run
 */
fun Application.module() {
    val conf = ConfigFactory.load().getConfig("glyph")

    install(AutoHeadResponse)

    install(DefaultHeaders)

    install(XForwardedHeaders)

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    install(Mustache) {
        mustacheFactory = DefaultMustacheFactory("content")
    }

    install(IgnoreTrailingSlash)

    install(Webjars)

    routing {
        documentation(conf.getString("discord.client-id"))
        staticContent()
    }
}

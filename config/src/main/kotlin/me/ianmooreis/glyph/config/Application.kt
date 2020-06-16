/*
 * Application.kt
 *
 * Glyph, a Discord bot that uses natural language instead of commands
 * powered by DialogFlow and Kotlin
 *
 * Copyright (C) 2017-2020 by Ian Moore
 *
 * This file is part of Glyph.
 *
 * Glyph is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.ianmooreis.glyph.config

import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.OAuthAccessTokenResponse
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.auth.oauth
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.features.origin
import io.ktor.locations.Locations
import io.ktor.request.host
import io.ktor.request.path
import io.ktor.request.port
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.netty.EngineMain
import io.ktor.sessions.SessionStorageMemory
import io.ktor.sessions.Sessions
import io.ktor.sessions.cookie
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import io.ktor.sessions.set
import me.ianmooreis.glyph.config.discord.DiscordOAuth2
import me.ianmooreis.glyph.config.discord.User
import org.slf4j.event.Level

/**
 * The entry point of the Ktor webs server
 */
fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module(testing: Boolean = false) {
    install(DefaultHeaders)
    install(Locations)
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }
    install(Authentication) {
        oauth("discord-oauth") {
            val discordAuth = DiscordOAuth2.getProvider(
                clientId = System.getenv("CLIENT_ID"),
                clientSecret = System.getenv("CLIENT_SECRET"),
                scopes = listOf("identify", "guilds")
            )

            client = HttpClient(OkHttp)
            providerLookup = { discordAuth }
            urlProvider = { redirectUrl("/login") }
        }
    }
    install(Sessions) {
        cookie<ConfigSession>("GlyphConfigSession", SessionStorageMemory())
    }

    routing {
        route("/") {
            get {
                val session = call.sessions.get<ConfigSession>()
                val token = session?.token

                if (token != null) {
                    val user = User.getUser(token)
                    val guilds = user.guilds.joinToString { it.name + if (it.hasManageGuild) "*" else "" }
                    call.respond("Hello ${user.username}, you're in $guilds")
                } else {
                    call.respond("Hello world!")
                }
            }
        }

        authenticate("discord-oauth") {
            route("/login") {
                handle {
                    val principal = call.authentication.principal<OAuthAccessTokenResponse.OAuth2>()
                        ?: error("No principal")

                    call.sessions.set(ConfigSession(principal.accessToken))

                    call.respondRedirect("/")
                }
            }
        }
    }
}

private fun ApplicationCall.redirectUrl(path: String): String {
    val defaultPort = if (request.origin.scheme == "http") 80 else 443
    val hostPort = request.host() + request.port().let { port -> if (port == defaultPort) "" else ":$port" }
    val protocol = request.origin.scheme
    return "$protocol://$hostPort$path"
}

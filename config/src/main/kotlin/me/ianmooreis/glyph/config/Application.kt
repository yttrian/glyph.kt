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

import com.github.mustachejava.DefaultMustacheFactory
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.oauth
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.features.AutoHeadResponse
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.features.XForwardedHeaderSupport
import io.ktor.features.origin
import io.ktor.mustache.Mustache
import io.ktor.mustache.MustacheContent
import io.ktor.request.host
import io.ktor.request.path
import io.ktor.request.port
import io.ktor.response.respond
import io.ktor.routing.IgnoreTrailingSlash
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.netty.EngineMain
import io.ktor.sessions.SessionStorageMemory
import io.ktor.sessions.Sessions
import io.ktor.sessions.cookie
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import me.ianmooreis.glyph.config.discord.DiscordOAuth2
import me.ianmooreis.glyph.config.discord.User
import me.ianmooreis.glyph.config.session.ConfigSession
import me.ianmooreis.glyph.shared.config.ConfigManager
import me.ianmooreis.glyph.shared.either.Either
import me.ianmooreis.glyph.shared.pubsub.PubSub
import me.ianmooreis.glyph.shared.pubsub.redis.RedisPubSub
import org.slf4j.event.Level

/**
 * The entry point of the Ktor webs server
 */
fun main(args: Array<String>): Unit = EngineMain.main(args)

/**
 * The main main module to run
 */
fun Application.module() {
    install(AutoHeadResponse)

    install(DefaultHeaders)

    install(XForwardedHeaderSupport)

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    install(Authentication) {
        oauth("discord-oauth") {
            val discordAuth = DiscordOAuth2.getProvider(
                clientId = System.getenv("DISCORD_CLIENT_ID"),
                clientSecret = System.getenv("DISCORD_CLIENT_SECRET"),
                scopes = listOf("identify", "guilds")
            )

            client = HttpClient(OkHttp)
            providerLookup = { discordAuth }
            urlProvider = { redirectUrl("/login") }
        }
    }

    install(Mustache) {
        mustacheFactory = DefaultMustacheFactory("content")
    }

    install(Sessions) {
        // TODO: Replace with a more robust storage method, do not currently trust SessionStorageRedis
        cookie<ConfigSession>("GlyphConfigSession", SessionStorageMemory())
    }

    install(IgnoreTrailingSlash)

    routing {
        get("/") {
            val token = call.sessions.get<ConfigSession>()?.accessToken

            val templateData = token?.let {
                when (val user = User.getUser(token)) {
                    is Either.Left -> null
                    is Either.Right -> mapOf("guilds" to user.r.guilds.mapNotNull {
                        if (it.hasManageGuild) mapOf("id" to it.id, "name" to it.name) else null
                    } + mapOf("id" to "logout", "name" to "Logout..."))
                }
            } ?: emptyMap()

            call.respond(MustacheContent("index.hbs", templateData))
        }

        val pubSub: PubSub = RedisPubSub {
            redisConnectionUri = System.getenv("REDIS_URL")
        }

        val configManager = ConfigManager {
            databaseConnectionUri = System.getenv("DATABASE_URL")
        }

        staticContent()

        editing(pubSub, configManager)

        discordLogin()
    }
}

private const val HTTP_PORT: Int = 80
private const val HTTPS_PORT: Int = 443

private fun ApplicationCall.redirectUrl(path: String): String {
    val defaultPort = if (request.origin.scheme == "http") HTTP_PORT else HTTPS_PORT
    val hostPort = request.host() + request.port().let { port -> if (port == defaultPort) "" else ":$port" }
    val protocol = request.origin.scheme
    return "$protocol://$hostPort$path"
}

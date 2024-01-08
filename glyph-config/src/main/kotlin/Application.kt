package org.yttr.glyph.config

import com.github.mustachejava.DefaultMustacheFactory
import com.typesafe.config.ConfigFactory
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
import org.slf4j.event.Level
import org.yttr.glyph.config.discord.DiscordOAuth2
import org.yttr.glyph.config.discord.User
import org.yttr.glyph.config.session.ConfigSession
import org.yttr.glyph.shared.Either
import org.yttr.glyph.shared.config.ConfigManager
import org.yttr.glyph.shared.pubsub.PubSub
import org.yttr.glyph.shared.pubsub.redis.RedisPubSub

/**
 * The entry point of the Ktor webs server
 */
fun main(args: Array<String>): Unit = EngineMain.main(args)

/**
 * The main main module to run
 */
fun Application.module() {
    val conf = ConfigFactory.load().getConfig("glyph")

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
                clientId = conf.getString("discord.client-id"),
                clientSecret = conf.getString("discord.client-secret"),
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
            redisConnectionUri = conf.getString("data.redis-url")
        }

        val configManager = ConfigManager {
            databaseConnectionUri = conf.getString("data.database-url")
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

package org.yttr.glyph.config

import com.github.mustachejava.DefaultMustacheFactory
import com.typesafe.config.ConfigFactory
import io.ktor.client.HttpClient
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.auth.authentication
import io.ktor.server.auth.oauth
import io.ktor.server.mustache.Mustache
import io.ktor.server.mustache.MustacheContent
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.autohead.AutoHeadResponse
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.plugins.forwardedheaders.XForwardedHeaders
import io.ktor.server.plugins.origin
import io.ktor.server.request.host
import io.ktor.server.request.path
import io.ktor.server.request.port
import io.ktor.server.response.respond
import io.ktor.server.routing.IgnoreTrailingSlash
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.sessions.Sessions
import io.ktor.server.sessions.cookie
import io.ktor.server.sessions.get
import io.ktor.server.sessions.maxAge
import io.ktor.server.sessions.sessions
import io.ktor.server.webjars.Webjars
import org.slf4j.event.Level
import org.yttr.glyph.config.discord.DiscordOAuth2
import org.yttr.glyph.config.discord.User
import org.yttr.glyph.config.session.ConfigSession
import org.yttr.glyph.config.session.SessionStorageRedis
import org.yttr.glyph.shared.Either
import org.yttr.glyph.shared.config.ConfigManager
import org.yttr.glyph.shared.pubsub.PubSub
import org.yttr.glyph.shared.pubsub.redis.RedisPubSub
import kotlin.time.Duration.Companion.minutes

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

    authentication {
        oauth("discord-oauth") {
            val discordAuth = DiscordOAuth2(
                clientId = conf.getString("discord.client-id"),
                clientSecret = conf.getString("discord.client-secret"),
                scopes = listOf("identify", "guilds")
            ).getProvider()

            client = HttpClient()
            providerLookup = { discordAuth }
            urlProvider = { redirectUrl("/login") }
        }
    }

    install(Mustache) {
        mustacheFactory = DefaultMustacheFactory("content")
    }

    install(Sessions) {
        cookie<ConfigSession>(
            name = "GlyphConfigSession",
            storage = SessionStorageRedis(redisUrl = conf.getString("data.redis-url"))
        ) {
            cookie.httpOnly = true
            cookie.maxAge = 30.minutes
            cookie.secure = true
        }
    }

    install(IgnoreTrailingSlash)

    install(Webjars)

    routing {
        documentation(conf.getString("discord.client-id"))

        route("/config") {
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

                call.respond(MustacheContent(template = "config.hbs", model = templateData))
            }

            val pubSub: PubSub = RedisPubSub(redisUrl = conf.getString("data.redis-url"))

            val configManager = ConfigManager(jdbcDatabaseUrl = conf.getString("data.database-url"))

            editing(pubSub, configManager)
        }

        staticContent()

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

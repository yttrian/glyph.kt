package org.yttr.glyph.config

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.mustache.MustacheContent
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.ContentTransformationException
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import kotlinx.serialization.json.Json
import org.yttr.glyph.config.discord.User
import org.yttr.glyph.config.session.ConfigSession
import org.yttr.glyph.shared.Either
import org.yttr.glyph.shared.config.ConfigManager
import org.yttr.glyph.shared.config.server.ServerConfig
import org.yttr.glyph.shared.pubsub.PubSub
import org.yttr.glyph.shared.pubsub.PubSubChannel
import org.yttr.glyph.shared.pubsub.PubSubException

/**
 * Endpoints for editing the config
 */
fun Route.editing(pubSub: PubSub, configManager: ConfigManager) {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }

    route("/{guildId}") {
        get {
            val guildId = call.parameters["guildId"] ?: error("No guild id given")
            call.respond(MustacheContent("edit.hbs", mapOf("guildId" to guildId)))
        }

        route("/data") {
            get {
                val guildId = call.parameters["guildId"] ?: error("No guild id given")
                val session = call.sessions.get<ConfigSession>()

                if (session.canManageGuild(guildId)) {
                    val response = when (val config = pubSub.ask(guildId, PubSubChannel.CONFIG_PREFIX)) {
                        is Either.Left -> HttpStatusCode.InternalServerError to when (config.l) {
                            is PubSubException.Deaf -> "Bot is completely offline, try again later."
                            is PubSubException.Ignored -> "Bot could not find the requested guild. Is it a member?"
                        }
                        is Either.Right -> HttpStatusCode.OK to config.r
                    }
                    call.respond(response.first, response.second)
                } else {
                    call.respond(HttpStatusCode.Unauthorized)
                }
            }

            post {
                val guildId = call.parameters["guildId"] ?: error("No guild id given")
                val session = call.sessions.get<ConfigSession>()

                if (session.canManageGuild(guildId)) {
                    val config = try {
                        call.receive<ServerConfig>()
                    } catch (e: ContentTransformationException) {
                        call.respond(HttpStatusCode.BadRequest, "That is not a valid server config JSON!")
                        error("Malformed server config JSON")
                    }
                    configManager.setServerConfig(guildId.toLong(), config)
                    pubSub.publish(PubSubChannel.CONFIG_REFRESH, guildId)
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.Unauthorized, "You do not have permission to do that!")
                }
            }
        }
    }
}

private fun User.canManageGuild(guildId: String): Boolean = this.guilds.any { it.id == guildId && it.hasManageGuild }

private suspend fun ConfigSession?.canManageGuild(guildId: String): Boolean =
    if (this == null) false else when (val user = User.getUser(accessToken)) {
        is Either.Left -> false
        is Either.Right -> user.r.canManageGuild(guildId)
    }

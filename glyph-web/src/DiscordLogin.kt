package org.yttr.glyph.config

import io.ktor.server.auth.OAuthAccessTokenResponse
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.authentication
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.sessions.clear
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.set
import org.yttr.glyph.config.session.ConfigSession
import java.time.Instant

/**
 * Endpoint for handling Discord OAuth2
 */
fun Route.discordLogin() {
    authenticate("discord-oauth") {
        route("/login") {
            handle {
                val principal = call.authentication.principal<OAuthAccessTokenResponse.OAuth2>()
                    ?: error("No principal")

                val session = ConfigSession(
                    principal.accessToken,
                    Instant.now().plusSeconds(principal.expiresIn).toEpochMilli()
                )
                call.sessions.set(session)

                call.respondRedirect("/config")
            }
        }
    }

    get("/logout") {
        call.sessions.clear<ConfigSession>()
        call.respondRedirect("/")
    }
}

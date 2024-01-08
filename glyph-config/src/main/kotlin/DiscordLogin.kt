package org.yttr.glyph.config

import io.ktor.application.call
import io.ktor.auth.OAuthAccessTokenResponse
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.response.respondRedirect
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.sessions.clear
import io.ktor.sessions.sessions
import io.ktor.sessions.set
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

                call.respondRedirect("/")
            }
        }
    }

    get("/logout") {
        call.sessions.clear<ConfigSession>()
        call.respondRedirect("/")
    }
}

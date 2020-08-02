/*
 * DiscordLogin.kt
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

import io.ktor.application.call
import io.ktor.auth.OAuthAccessTokenResponse
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.response.respondRedirect
import io.ktor.routing.Route
import io.ktor.routing.route
import io.ktor.sessions.sessions
import io.ktor.sessions.set
import me.ianmooreis.glyph.config.session.ConfigSession
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
}

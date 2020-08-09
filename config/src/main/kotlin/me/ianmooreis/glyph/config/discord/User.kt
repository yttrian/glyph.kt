/*
 * DiscordUser.kt
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

package me.ianmooreis.glyph.config.discord

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.left
import arrow.core.right
import io.ktor.client.HttpClient
import io.ktor.client.features.ClientRequestException
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header

/**
 * Represents a Discord user
 */
data class User(
    /**
     * The user snowflake id
     */
    val id: Long,
    /**
     * The user's name
     */
    val username: String,
    /**
     * Guilds the user belongs to
     */
    val guilds: List<UserGuild>
) {
    companion object {
        private val client: HttpClient = HttpClient {
            install(JsonFeature)
        }

        private const val USER_API_BASE: String = "https://discord.com/api/users/@me"

        /**
         * Get a user, based on
         */
        suspend fun getUser(token: Option<String>): Either<DiscordException, User> = when (token) {
            is Some -> try {
                fun HttpRequestBuilder.addAuth() = header("Authorization", "Bearer ${token.t}")
                val user: User = client.get(USER_API_BASE) { addAuth() }
                val guilds: List<UserGuild> = client.get("$USER_API_BASE/guilds") { addAuth() }

                User(user.id, user.username, guilds).right()
            } catch (e: ClientRequestException) {
                DiscordException.Unauthorized.left()
            }
            is None -> DiscordException.InvalidToken.left()
        }
    }
}

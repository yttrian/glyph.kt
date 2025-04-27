package org.yttr.glyph.config.discord

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.yttr.glyph.shared.Either
import org.yttr.glyph.shared.left
import org.yttr.glyph.shared.right

/**
 * Represents a Discord user
 */
@Serializable
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
    val guilds: List<UserGuild> = emptyList()
) {
    companion object {
        private val client: HttpClient = HttpClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        private const val USER_API_BASE: String = "https://discord.com/api/users/@me"

        /**
         * Get a user, based on
         */
        suspend fun getUser(token: String): Either<DiscordException, User> = when {
            token.isNotBlank() -> try {
                fun HttpRequestBuilder.addAuth() = header("Authorization", "Bearer $token")
                val user: User = client.get(USER_API_BASE) { addAuth() }.body()
                val guilds: List<UserGuild> = client.get("$USER_API_BASE/guilds") { addAuth() }.body()

                User(user.id, user.username, guilds).right()
            } catch (e: ClientRequestException) {
                DiscordException.Unauthorized.left()
            }
            else -> DiscordException.InvalidToken.left()
        }
    }
}

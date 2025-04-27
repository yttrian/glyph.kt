package org.yttr.glyph.config.discord

import io.ktor.http.HttpMethod
import io.ktor.server.auth.OAuthServerSettings
import io.ktor.util.StatelessHmacNonceManager
import java.security.SecureRandom

/**
 * Assists in the usage of the Discord OAuth2 endpoints
 */
class DiscordOAuth2(
    private val clientId: String,
    private val clientSecret: String,
    private val scopes: List<String> = listOf("identify")
) {
    /**
     * The OAuth2 provider
     */
    fun getProvider(): OAuthServerSettings.OAuth2ServerSettings = OAuthServerSettings.OAuth2ServerSettings(
        name = "discord",
        authorizeUrl = "https://discord.com/api/oauth2/authorize",
        accessTokenUrl = "https://discord.com/api/oauth2/token",
        requestMethod = HttpMethod.Post,
        clientId = clientId,
        clientSecret = clientSecret,
        defaultScopes = scopes,
        nonceManager = StatelessHmacNonceManager(key = generateRandomKey())
    )

    /**
     * We don't need to preserve this value across restarts
     */
    private fun generateRandomKey(): ByteArray {
        val random = SecureRandom()
        val key = ByteArray(KEY_LENGTH)
        random.nextBytes(key)
        return key
    }

    companion object {
        private const val KEY_LENGTH: Int = 32
    }
}

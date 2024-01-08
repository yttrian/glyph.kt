package org.yttr.glyph.config.discord

import io.ktor.auth.OAuthServerSettings
import io.ktor.http.HttpMethod

/**
 * Assists in the usage of the Discord OAuth2 endpoints
 */
object DiscordOAuth2 {
    /**
     * The OAuth2 provider
     */
    fun getProvider(
        clientId: String,
        clientSecret: String,
        scopes: List<String> = listOf("identify")
    ): OAuthServerSettings.OAuth2ServerSettings = OAuthServerSettings.OAuth2ServerSettings(
        name = "discord",
        authorizeUrl = "https://discord.com/api/oauth2/authorize",
        accessTokenUrl = "https://discord.com/api/oauth2/token",
        requestMethod = HttpMethod.Post,
        clientId = clientId,
        clientSecret = clientSecret,
        defaultScopes = scopes
    )
}

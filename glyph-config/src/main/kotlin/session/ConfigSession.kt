package org.yttr.glyph.config.session

/**
 * Represents a session on the config editor
 */
data class ConfigSession(
    /**
     * The current access token
     */
    val accessToken: String,
    /**
     * Epoch milli for when the token expires
     */
    val tokenExpiration: Long
)

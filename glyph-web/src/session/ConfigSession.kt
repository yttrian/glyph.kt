package org.yttr.glyph.config.session

import kotlinx.serialization.Serializable

/**
 * Represents a session on the config editor
 */
@Serializable
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

package org.yttr.glyph.presentation

/**
 * A bot list website's API for tracking statistics
 */
data class BotList(
    /**
     * The name of the bot list website
     */
    val name: String,
    /**
     * The API endpoint for the bot list website
     */
    val apiEndpoint: String,
    /**
     * The token for authenticating the request to the bot list website
     */
    val token: String
)

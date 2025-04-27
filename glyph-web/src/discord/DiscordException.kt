package org.yttr.glyph.config.discord

/**
 * Discord API related exceptions
 */
sealed class DiscordException : Exception() {
    /**
     * The request lacked proper authorization
     */
    object Unauthorized : DiscordException()

    /**
     * The token used is invalid
     */
    object InvalidToken : DiscordException()

    /**
     * The token used is expired
     */
    object ExpiredToken : DiscordException()
}

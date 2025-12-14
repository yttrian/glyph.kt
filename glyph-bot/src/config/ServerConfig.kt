package org.yttr.glyph.shared.config

abstract class ServerConfig {
    data class Starboard(
        /**
         * Is the starboard enabled?
         */
        val enabled: Boolean = false,
        /**
         * The channel to send starred messages to
         */
        val channel: Long? = null,
        /**
         * The emoji to check for when starboarding
         */
        val emoji: String = "star",
        /**
         * The minimum number of reactions of the check emoji needed before the message is sent to the starboard
         */
        val threshold: Int = 1,
        /**
         * Whether members can star their own messages
         */
        val canSelfStar: Boolean = false
    )

    data class QuickView(
        val furAffinityEnabled: Boolean = true,
    )

    abstract suspend fun getStarboardConfig(): Starboard
    abstract suspend fun getQuickViewConfig(): QuickView
}

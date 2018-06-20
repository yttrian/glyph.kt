package me.ianmooreis.glyph.configs

/**
 * A configuration for starboarding
 */
data class StarboardConfig(
    /**
     * Whether or not the starboard is enabled
     */
    val enabled: Boolean = false,
    /**
     * The webhook to send starred messages to
     */
    val webhook: String? = null,
    /**
     * The emoji to check for when starboarding
     */
    val emoji: String = "star",
    /**
     * The minimum number of reactions of the check emoji needed before the message is sent to the starboard
     */
    val threshold: Int = 1,
    /**
     * Whether or not members can star their own messages
     */
    val allowSelfStarring: Boolean = false)
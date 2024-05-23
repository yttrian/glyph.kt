package org.yttr.glyph.config.server

import org.yttr.glyph.config.Config

/**
 * A configuration for starboarding
 */
@Serializable
data class StarboardConfig(
    /**
     * Whether or not the starboard is enabled
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
     * Whether or not members can star their own messages
     */
    val allowSelfStarring: Boolean = false
) : Config

package org.yttr.glyph.bot.ai

/**
 * Fulfillment data directly from the agent
 */
interface AIFulfillment {
    /**
     * What the agent wants to say
     */
    val speech: String
}

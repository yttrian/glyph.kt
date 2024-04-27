package org.yttr.glyph.bot.ai

/**
 * An AI agent that is capable of processing messages
 */
abstract class AIAgent(
    /**
     * Friendly name of the service
     */
    val name: String
) {
    /**
     * Request an AIResponse for a message from the agent
     */
    abstract fun request(message: String, sessionId: String): AIResponse
}

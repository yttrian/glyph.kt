package org.yttr.glyph.ai

/**
 * The response from the agent service
 */
interface AIResponse {
    /**
     * The session id used when sending the message
     */
    val sessionID: String

    /**
     * If an error occurred while detecting the intent
     */
    val isError: Boolean

    /**
     * Result of the agent's interpretation
     */
    val result: AIResult

}

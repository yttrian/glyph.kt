package org.yttr.glyph.ai

import com.google.gson.JsonObject

/**
 * Result of the agent's interpretation
 */
interface AIResult {
    /**
     * The action (skill) that the agent detected
     */
    val action: String

    /**
     * Whether or not the action is expecting an immediate follow-up
     */
    val isActionIncomplete: Boolean

    /**
     * Agent side fulfillment results (like messages to say)
     */
    val fulfillment: AIFulfillment

    /**
     * Get the value of a string parameter
     */
    fun getComplexParameter(parameterName: String): JsonObject?

    /**
     * Get the value of a string parameter
     */
    fun getStringParameter(parameterName: String): String?
}

/*
 * AIResult.kt
 *
 * Glyph, a Discord bot that uses natural language instead of commands
 * powered by DialogFlow and Kotlin
 *
 * Copyright (C) 2017-2020 by Ian Moore
 *
 * This file is part of Glyph.
 *
 * Glyph is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.yttr.glyph.bot.ai

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

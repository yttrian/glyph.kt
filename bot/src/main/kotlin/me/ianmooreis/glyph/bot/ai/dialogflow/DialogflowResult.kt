/*
 * DialogflowResult.kt
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

package me.ianmooreis.glyph.bot.ai.dialogflow

import com.google.cloud.dialogflow.v2.QueryResult
import com.google.gson.JsonObject
import com.google.protobuf.Value
import me.ianmooreis.glyph.bot.ai.AIResult

/**
 * A wrapper for the new DialogFlow API v2 results
 */
class DialogflowResult(private val result: QueryResult) : AIResult {
    /**
     * The action (skill) that the agent detected
     */
    override val action: String = result.action

    /**
     * Whether or not the action is expecting an immediate follow-up
     */
    override val isActionIncomplete: Boolean = false // TODO: Find value

    /**
     * DialogFlow side fulfillment results (like messages to say)
     */
    override val fulfillment = DialogflowFulfillment(result)

    private fun getField(key: String): Value {
        return result.parameters.getFieldsOrDefault(key, Value.getDefaultInstance())
    }

    /**
     * Get the value of a string parameter
     */
    override fun getComplexParameter(parameterName: String): JsonObject? {
        val struct = getField(parameterName).structValue
        val json = JsonObject()

        struct.fieldsMap.forEach { (key, value) ->
            if (value.stringValue.isNotEmpty()) {
                json.addProperty(key, value.stringValue)
            } else {
                json.addProperty(key, value.numberValue)
            }
        }

        return json
    }

    /**
     * Get the value of a string parameter
     */
    override fun getStringParameter(parameterName: String): String? {
        val value = getField(parameterName).stringValue

        return if (value.isNotEmpty()) value else null
    }
}
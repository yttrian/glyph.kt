package org.yttr.glyph.bot.ai.dialogflow

import com.google.cloud.dialogflow.v2.QueryResult
import com.google.gson.JsonObject
import com.google.protobuf.Value
import org.yttr.glyph.bot.ai.AIResult

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

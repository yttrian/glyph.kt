package org.yttr.glyph.bot.ai.dialogflow

import com.google.cloud.dialogflow.v2.QueryResult
import org.yttr.glyph.bot.ai.AIFulfillment

/**
 * A wrapper for the new DialogFlow API v2 fulfillment
 */
class DialogflowFulfillment(result: QueryResult) : AIFulfillment {
    /**
     * What the agent wants to say
     */
    override val speech: String = result.fulfillmentText.replace("\\n", "\n", ignoreCase = true).trim()
}

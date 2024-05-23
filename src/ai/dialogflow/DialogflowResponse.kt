package org.yttr.glyph.ai.dialogflow

import com.google.cloud.dialogflow.v2.DetectIntentResponse
import org.yttr.glyph.ai.AIResponse

/**
 * A wrapper for the new DialogFlow API v2 responses
 */
class DialogflowResponse(response: DetectIntentResponse, override val sessionID: String) : AIResponse {
    /**
     * If an error occurred while detecting the intent
     */
    override val isError: Boolean = !response.hasQueryResult()

    /**
     * The result from DialogFlow
     */
    override val result: DialogflowResult = DialogflowResult(response.queryResult)
}

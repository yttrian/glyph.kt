/*
 * Dialogflow.kt
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

package me.ianmooreis.glyph.ai.dialogflow

import com.google.api.gax.core.FixedCredentialsProvider
import com.google.auth.oauth2.GoogleCredentials
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.dialogflow.v2.DetectIntentResponse
import com.google.cloud.dialogflow.v2.QueryInput
import com.google.cloud.dialogflow.v2.SessionName
import com.google.cloud.dialogflow.v2.SessionsClient
import com.google.cloud.dialogflow.v2.SessionsSettings
import com.google.cloud.dialogflow.v2.TextInput
import me.ianmooreis.glyph.ai.AIAgent
import me.ianmooreis.glyph.ai.AIResponse
import java.io.ByteArrayInputStream
import java.net.UnknownHostException

/**
 * Wrapper for the new DialogFlow API v2
 */
class Dialogflow(
    credentialStream: ByteArrayInputStream,
    configure: Config.() -> Unit = {}
) : AIAgent("Dialogflow") {
    /**
     * HOCON-like config for the Dialogflow agent
     */
    class Config {
        /**
         * The language code used during requests
         */
        var languageCode: String = "en-US" // TODO: Not hardcode language
    }

    private val config = Config().also(configure)
    private val credentials: GoogleCredentials = GoogleCredentials.fromStream(credentialStream)
    private val agent: SessionsClient = run {
        val sessionSettings = SessionsSettings.newBuilder()
            .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
            .build()

        SessionsClient.create(sessionSettings)
    }
    private val projectId = (credentials as ServiceAccountCredentials).projectId
    private val errorResponse = DialogflowResponse(DetectIntentResponse.getDefaultInstance(), "None")

    /**
     * Request an AIResponse for a message from the agent
     */
    override fun request(message: String, sessionId: String): AIResponse {
        val textInput = TextInput.newBuilder().setText(message).setLanguageCode(config.languageCode)
        val queryInput = QueryInput.newBuilder().setText(textInput).build()
        val session = SessionName.of(projectId, sessionId)

        return try {
            val response = agent.detectIntent(session, queryInput)
            DialogflowResponse(response, sessionId)
        } catch (e: UnknownHostException) {
            errorResponse
        }
    }
}

/*
 * DialogFlow.kt
 *
 * Glyph, a Discord bot that uses natural language instead of commands
 * powered by DialogFlow and Kotlin
 *
 * Copyright (C) 2017-2019 by Ian Moore
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

package me.ianmooreis.glyph.directors.messaging

import com.google.api.gax.core.FixedCredentialsProvider
import com.google.auth.oauth2.GoogleCredentials
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.dialogflow.v2.*

/**
 * Wrapper for the new DialogFlow API v2
 */
object DialogFlow {
    private val credentials = getCredentials()
    private val agent = createClient()
    private val projectId = getProjectId()

    private fun getCredentials(): GoogleCredentials {
        val credentialStream = System.getenv("DIALOGFLOW_CREDENTIALS").byteInputStream()

        return GoogleCredentials.fromStream(credentialStream)
    }

    private fun createClient(): SessionsClient {
        val sessionSettings = SessionsSettings.newBuilder()
            .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
            .build()

        return SessionsClient.create(sessionSettings)
    }

    private fun getProjectId(): String {
        return (credentials as ServiceAccountCredentials).projectId
    }


    /**
     * Request an AIResponse for a message from the agent
     */
    fun request(message: String, sessionId: String): AIResponse {
        val textInput = TextInput.newBuilder().setText(message).setLanguageCode("en-US") // TODO: Not hardcode language
        val queryInput = QueryInput.newBuilder().setText(textInput).build()
        val session = SessionName.of(projectId, sessionId)
        val response = agent.detectIntent(session, queryInput)

        return AIResponse(response, sessionId)
    }
}
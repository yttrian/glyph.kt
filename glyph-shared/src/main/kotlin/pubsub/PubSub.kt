/*
 * PubSub.kt
 *
 * Glyph, a Discord bot that uses natural language instead of commands
 * powered by DialogFlow and Kotlin
 *
 * Copyright (C) 2017-2021 by Ian Moore
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

package org.yttr.glyph.shared.pubsub

import org.yttr.glyph.shared.Either

/**
 * Generic interface for PubSub connectors
 */
interface PubSub {
    /**
     * Publish a message
     */
    fun publish(channel: PubSubChannel, message: String)

    /**
     * Add an action-less listener
     */
    fun addListener(listenChannel: PubSubChannel, action: (message: String) -> Unit)

    /**
     * Publish a message and listen for the response
     */
    suspend fun ask(query: String, askChannelPrefix: PubSubChannel): Either<PubSubException, String>

    /**
     * Add a responder for an ask
     */
    fun addResponder(askChannelPrefix: PubSubChannel, responder: (message: String) -> String?)
}

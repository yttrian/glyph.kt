/*
 * Response.kt
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

package me.ianmooreis.glyph.messaging

import net.dv8tion.jda.api.entities.MessageEmbed
import java.time.Duration

/**
 * A way Glyph can respond to a message
 */
sealed class Response

/**
 * A response that only lasts a limited about of time
 */
data class EphemeralResponse(
    /**
     * The message content
     */
    val content: String? = null,
    /**
     * The message embed
     */
    val embed: MessageEmbed? = null,
    /**
     * The time to live for the message before being deleted
     */
    val ttl: Duration
) : Response()

/**
 * A message that will delete itself when the triggering message is also deleted
 */
data class VolatileResponse(
    /**
     * The message content
     */
    val content: String? = null,
    /**
     * The message embed
     */
    val embed: MessageEmbed? = null
) : Response()

/**
 * A message that will not be automatically deleted
 */
data class PermanentResponse(
    /**
     * The message content
     */
    val content: String? = null,
    /**
     * The message embed
     */
    val embed: MessageEmbed? = null
) : Response()

/**
 * Emoji react in response to a message
 */
data class ReactionResponse(
    /**
     * The emoji to react with
     */
    val emoji: String
) : Response()

/**
 * Do not respond to a message
 */
object NoResponse : Response()
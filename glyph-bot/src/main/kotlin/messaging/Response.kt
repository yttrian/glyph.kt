/*
 * Response.kt
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

package org.yttr.glyph.bot.messaging

import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import java.time.Duration

/**
 * A way Glyph can respond to a message
 */
sealed class Response {
    /**
     * A message type response
     */
    abstract class MessageResponse : Response() {
        /**
         * The message content
         */
        abstract val content: String?

        /**
         * The message embed
         */
        abstract val embed: MessageEmbed?

        /**
         * JDA message
         */
        val message: Message by lazy {
            val builder = MessageBuilder()
            content?.let { builder.setContent(it.trim()) }
            embed?.let { builder.setEmbeds(it) }
            builder.build()
        }
    }

    /**
     * A response that only lasts a limited about of time
     */
    data class Ephemeral(
        override val content: String? = null,
        override val embed: MessageEmbed? = null,
        /**
         * The time to live for the message before being deleted
         */
        val ttl: Duration
    ) : MessageResponse() {
        constructor(content: String, ttl: Duration) : this(content, null, ttl)
        constructor(embed: MessageEmbed, ttl: Duration) : this(null, embed, ttl)
    }

    /**
     * A message that will delete itself when the triggering message is also deleted
     */
    data class Volatile(
        override val content: String? = null,
        override val embed: MessageEmbed? = null
    ) : MessageResponse() {
        constructor(embed: MessageEmbed) : this(null, embed)
    }

    /**
     * A message that will not be automatically deleted
     */
    data class Permanent(
        override val content: String? = null,
        override val embed: MessageEmbed? = null
    ) : MessageResponse() {
        constructor(embed: MessageEmbed) : this(null, embed)
    }

    /**
     * Emoji react in response to a message
     */
    data class Reaction(
        /**
         * The emoji to react with
         */
        val emoji: String
    ) : Response()

    /**
     * Do not respond to a message
     */
    object None : Response()
}

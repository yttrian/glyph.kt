package org.yttr.glyph.bot.messaging

import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.interactions.components.ActionRow
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
         * An action row to attach to the message
         */
        abstract val actionRow: ActionRow?

        /**
         * JDA message
         */
        val message: Message by lazy {
            val builder = MessageBuilder()
            content?.let { builder.setContent(it.trim()) }
            embed?.let { builder.setEmbeds(it) }
            actionRow?.let { builder.setActionRows(actionRow) }
            builder.build()
        }
    }

    /**
     * A response that only lasts a limited about of time
     */
    data class Ephemeral(
        override val content: String? = null,
        override val embed: MessageEmbed? = null,
        override val actionRow: ActionRow? = null,
        /**
         * The time to live for the message before being deleted
         */
        val ttl: Duration
    ) : MessageResponse() {
        constructor(content: String, ttl: Duration) : this(content, null, null, ttl)
        constructor(embed: MessageEmbed, ttl: Duration) : this(null, embed, null, ttl)
    }

    /**
     * A message that will delete itself when the triggering message is also deleted
     */
    data class Volatile(
        override val content: String? = null,
        override val embed: MessageEmbed? = null,
        override val actionRow: ActionRow? = null
    ) : MessageResponse() {
        constructor(embed: MessageEmbed, actionRow: ActionRow? = null) : this(null, embed, actionRow)
    }

    /**
     * A message that will not be automatically deleted
     */
    data class Permanent(
        override val content: String? = null,
        override val embed: MessageEmbed? = null,
        override val actionRow: ActionRow? = null
    ) : MessageResponse() {
        constructor(embed: MessageEmbed, actionRow: ActionRow? = null) : this(null, embed, actionRow)
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

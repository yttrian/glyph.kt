/*
 * MessagingDirector *
 * Glyph, a Discord bot that uses natural language instead of commands
 * powered by DialogFlow and Kotlin
 *
 * Copyright (C) 2017-2018 by Ian Moore
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

import kotlinx.coroutines.launch
import me.ianmooreis.glyph.Director
import me.ianmooreis.glyph.ai.AIAgent
import me.ianmooreis.glyph.database.Key
import me.ianmooreis.glyph.directors.StatusDirector
import me.ianmooreis.glyph.directors.skills.SkillDirector
import me.ianmooreis.glyph.extensions.contentClean
import me.ianmooreis.glyph.messaging.response.EphemeralResponse
import me.ianmooreis.glyph.messaging.response.PermanentResponse
import me.ianmooreis.glyph.messaging.response.ReactionResponse
import me.ianmooreis.glyph.messaging.response.VolatileResponse
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.message.MessageDeleteEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import org.apache.commons.codec.digest.DigestUtils
import redis.clients.jedis.JedisPool
import java.time.Duration
import java.util.concurrent.TimeUnit

/**
 * Manages message events including handling incoming messages and dispatching the SkillDirector in addition to the message ledger
 */
class MessagingDirector(
    private val aiAgent: AIAgent,
    private val redisPool: JedisPool,
    configure: Config.() -> Unit = {}
) : Director() {
    /**
     * HOCON-like config for the messaging director
     */
    class Config {
        /**
         * How long associated messages should be remembered for DeleteWith functionality
         */
        var volatileTrackingExpiration: Duration = Duration.ofDays(14)
    }

    private val config = Config().also(configure)
    private val volatileTrackingExpirationSeconds = config.volatileTrackingExpiration.toSeconds().toInt()

    /**
     * Add a message to the ledger
     *
     * @param invokerId  the message id the invoked the response message
     * @param responseId the message id of the response message to the invoking message
     */
    private fun trackVolatile(invokerId: String, responseId: String): Unit = redisPool.resource.use {
        it.setex(Key.VOLATILE_MESSAGE_PREFIX.value + invokerId, volatileTrackingExpirationSeconds, responseId)
    }

    /**
     * Log a failure to send a message, useful so figuring out the if someone complains Glyph won't respond
     *
     * @param channel the channel where the message failed to send
     */
    private fun logSendFailure(channel: TextChannel) {
        if (channel.type.isGuild) {
            log.warn("Failed to send message in $channel of ${channel.guild}!")
        } else {
            log.warn("Failed to send message in $channel!.")
        }
    }

    /**
     * When a new message is seen anywhere
     */
    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.isIgnorable) return

        val message: Message = event.message

        // Get ready to ask the DialogFlow agent
        val sessionId = DigestUtils.md5Hex(event.author.id + event.channel.id)
        val ai = aiAgent.request(event.message.contentClean, sessionId)

        // In the rare circumstance DialogFlow is unavailable, warn the user
        if (ai.isError) {
            message.reply("It appears DialogFlow is currently unavailable, please try again later!")
            StatusDirector.setPresence(
                event.jda,
                OnlineStatus.DO_NOT_DISTURB,
                Activity.watching("temporary outage at DialogFlow")
            )
            return
        }

        // Assuming everything else went well, launch the appropriate skill with the event info and ai response
        SkillDirector.launch {
            when (val response = SkillDirector.trigger(event, ai)) {
                is EphemeralResponse -> message.reply(response.content, response.embed, ttl = response.ttl)
                is VolatileResponse -> message.reply(response.content, response.embed, volatile = true)
                is PermanentResponse -> message.reply(response.content, response.embed, volatile = false)
                is ReactionResponse -> message.addReaction(response.emoji)
            }

            // Increment the total message count for curiosity's sake
            redisPool.resource.use {
                it.incr(Key.MESSAGE_COUNT.value)
            }
        }
    }

    /**
     * When a message is deleted anywhere, remove the invoking message if considered volatile
     */
    override fun onMessageDelete(event: MessageDeleteEvent): Unit = redisPool.resource.use { redis ->
        val key = Key.VOLATILE_MESSAGE_PREFIX.value + event.messageId
        redis.get(key)?.let { responseId ->
            redis.del(key)
            event.channel.retrieveMessageById(responseId).queue {
                it.addReaction("❌").queue()
                it.delete().queueAfter(1, TimeUnit.SECONDS)
            }
        }
    }

    private val MessageReceivedEvent.isIgnorable
        get() = this.author.isBot ||  // ignore other bots
            (this.author == this.jda.selfUser) ||  // ignore self
            this.isWebhookMessage ||  // ignore webhooks
            (this.isFromGuild && !message.isMentioned(this.jda.selfUser)) ||  // require mention except in DMs
            message.contentClean.isEmpty()  // ignore empty messages
    // ((!message.isMentioned(this.jda.selfUser) || (message.contentStripped.trim() == message.contentClean)) && this.channelType.isGuild)

    private fun Message.reply(
        content: String? = null,
        embed: MessageEmbed? = null,
        ttl: Duration? = null,
        volatile: Boolean = true
    ) {
        // require some content
        if (content == null && embed == null) return
        // build the message
        val message = MessageBuilder().setContent(content?.trim()).setEmbed(embed).build()
        // try to send the message
        try {
            this.channel.sendMessage(message).queue {
                if (ttl != null) {
                    it.delete().queueAfter(ttl.seconds, TimeUnit.SECONDS)
                } else if (volatile) {
                    trackVolatile(this.id, it.id)
                }
            }
        } catch (e: InsufficientPermissionException) {
            logSendFailure(this.textChannel)
        }
    }
}


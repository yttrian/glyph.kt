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
import me.ianmooreis.glyph.ai.AIAgent
import me.ianmooreis.glyph.database.Key
import me.ianmooreis.glyph.directors.StatusDirector
import me.ianmooreis.glyph.directors.skills.SkillDirector
import me.ianmooreis.glyph.extensions.contentClean
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.message.MessageDeleteEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.jodah.expiringmap.ExpiringMap
import org.apache.commons.codec.digest.DigestUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
) : ListenerAdapter() {
    /**
     * HOCON-like config for the messaging director
     */
    class Config {
        /**
         * How long associated messages should be remembered for DeleteWith functionality
         */
        var deleteWithExpiration: Duration = Duration.ofDays(14)
    }

    private val config = Config().also(configure)
    private val deleteWithExpirationSeconds = config.deleteWithExpiration.toSeconds().toInt()
    private val log: Logger = LoggerFactory.getLogger(this.javaClass.simpleName)
    private val ledger: MutableMap<Long, Long> = ExpiringMap.builder().expiration(1, TimeUnit.HOURS).build()

    /**
     * Add a message to the ledger
     *
     * @param invokerId  the message id the invoked the response message
     * @param responseId the message id of the response message to the invoking message
     */
    private fun amendLedger(invokerId: String, responseId: String): Unit = redisPool.resource.use {
        it.setex(Key.DELETE_WITH_PREFIX.value + invokerId, deleteWithExpirationSeconds, responseId)
    }

    /**
     * Log a failure to send a message, useful so figuring out the if someone complains Glyph won't respond
     *
     * @param channel the channel where the message failed to send
     */
    fun logSendFailure(channel: TextChannel) {
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
        val message: Message = event.message

        // Ignore self, other bots, and webhooks
        if (event.author.isBot or (event.author == event.jda.selfUser) or event.isWebhookMessage) return
        // Require a mention in a server, but not in a PM
        if ((!message.isMentioned(event.jda.selfUser) or (message.contentStripped.trim() == message.contentClean)) and event.message.channelType.isGuild) return
        // Ignore empty messages
        if (event.message.contentClean.isEmpty()) {
            event.message.reply("You have to say something!")
            return
        }

        // Get ready to ask the DialogFlow agent
        val sessionId = DigestUtils.md5Hex(event.author.id + event.channel.id)
        val ai = aiAgent.request(event.message.contentClean, sessionId)
        // In the rare circumstance DialogFlow is unavailable, warn the user
        if (ai.isError) {
            event.message.reply("It appears DialogFlow is currently unavailable, please try again later!")
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
                is FormalResponse -> message.reply(
                    response.content,
                    response.embed,
                    response.deleteAfterDelay,
                    response.deleteAfterUnit,
                    response.deleteWithEnabled
                )
            }

            // Increment the total message count for curiosity's sake
            redisPool.resource.use {
                it.incr(Key.MESSAGE_COUNT.value)
            }
        }
    }

    /**
     * When a message is deleted anywhere
     */
    override fun onMessageDelete(event: MessageDeleteEvent): Unit = redisPool.resource.use { redis ->
        val key = Key.DELETE_WITH_PREFIX.value + event.messageId
        val responseId: String? = redis.get(key)

        if (responseId != null) {
            redis.del(key)
            event.channel.retrieveMessageById(responseId).queue {
                it.addReaction("❌").queue()
                it.delete().queueAfter(1, TimeUnit.SECONDS)
            }
        }
    }

    /**
     * Reply to a message
     *
     * @param content the reply body
     * @param embed an embed to include in the message
     * @param deleteAfterDelay how long to wait before automatically deleting the message (if ever)
     * @param deleteAfterUnit the time units the deleteAfterDelay used
     * @param deleteWithEnabled whether or not to delete the response when the invoking message is deleted
     */
    fun Message.reply(
        content: String? = null,
        embed: MessageEmbed? = null,
        deleteAfterDelay: Long = 0,
        deleteAfterUnit: TimeUnit = TimeUnit.SECONDS,
        deleteWithEnabled: Boolean = true
    ) {
        if (content == null && embed == null) {
            return
        }
        val message = MessageBuilder().setContent(content?.trim()).setEmbed(embed).build()
        try {
            this.channel.sendMessage(message).queue {
                if (deleteAfterDelay > 0) {
                    it.delete().queueAfter(deleteAfterDelay, deleteAfterUnit)
                } else if (deleteWithEnabled) {
                    amendLedger(this.id, it.id)
                }
            }
        } catch (e: InsufficientPermissionException) {
            logSendFailure(this.textChannel)
        }
    }

    /**
     * Reply to a message with an embed
     *
     * @param embed the embed to send
     * @param deleteAfterDelay how long to wait before automatically deleting the message (if ever)
     * @param deleteAfterUnit the time units the deleteAfterDelay used
     * @param deleteWithEnabled whether or not to delete the response when the invoking message is deleted
     */
    fun Message.reply(
        embed: MessageEmbed,
        deleteAfterDelay: Long = 0,
        deleteAfterUnit: TimeUnit = TimeUnit.SECONDS,
        deleteWithEnabled: Boolean = true
    ) {
        this.reply(
            content = null,
            embed = embed,
            deleteAfterDelay = deleteAfterDelay,
            deleteAfterUnit = deleteAfterUnit,
            deleteWithEnabled = deleteWithEnabled
        )
    }
}


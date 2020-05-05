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

package me.ianmooreis.glyph.directors.messaging

import me.ianmooreis.glyph.ai.AIAgent
import me.ianmooreis.glyph.directors.StatusDirector
import me.ianmooreis.glyph.directors.skills.SkillDirector
import me.ianmooreis.glyph.extensions.contentClean
import me.ianmooreis.glyph.extensions.reply
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Emote
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.message.MessageDeleteEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.jodah.expiringmap.ExpiringMap
import org.apache.commons.codec.digest.DigestUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

/**
 * Manages message events including handling incoming messages and dispatching the SkillDirector in addition to the message ledger
 */
class MessagingDirector(private val aiAgent: AIAgent) : ListenerAdapter() {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass.simpleName)

    private val ledger: MutableMap<Long, Long> = ExpiringMap.builder().expiration(1, TimeUnit.HOURS).build()
    private var totalMessages: Int = 0
    private val customEmotes: MutableMap<String, Emote> = mutableMapOf()

    /**
     * Attempts to grab a custom emote by name from the emoji server
     */
    fun getCustomEmote(name: String): Emote? {
        return customEmotes[name]
    }

    private fun loadCustomEmotes(guild: Guild) {
        if (customEmotes.isEmpty()) {
            customEmotes.putAll(guild.emotes.map {
                it.name to it
            })
        }
    }

    /**
     * Add a message to the ledger
     *
     * @param invoker  the message id the invoked the response message
     * @param response the message id of the response message to the invoking message
     */
    fun amendLedger(invoker: Long, response: Long) {
        ledger[invoker] = response
    }

    /**
     * Gets the total number of messages received and reacted upon since startup
     *
     * @returns the total number of messages received and reacted upon since startup
     */
    fun getTotalMessages(): Int {
        return totalMessages
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
     * When the client is ready, perform any needed tasks
     */
    override fun onReady(event: ReadyEvent) {
        event.jda.getGuildById(System.getenv("EMOJI_GUILD"))?.let { loadCustomEmotes(it) }
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
        SkillDirector.trigger(event, ai)

        // Increment the total message count for curiosity's sake
        totalMessages++
    }

    /**
     * When a message is deleted anywhere
     */
    override fun onMessageDelete(event: MessageDeleteEvent) {
        val messageId = ledger[event.messageIdLong]
        if (messageId != null) {
            event.channel.retrieveMessageById(messageId).queue {
                it.addReaction("❌").queue()
                it.delete().queueAfter(1, TimeUnit.SECONDS)
                ledger.remove(it.idLong)
            }
        }
    }
}


/*
 * MessagingOrchestrator.kt
 *
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

package me.ianmooreis.glyph.orchestrators.messaging

import ai.api.AIConfiguration
import ai.api.AIDataService
import ai.api.AIServiceContextBuilder
import ai.api.model.AIRequest
import kotlinx.coroutines.experimental.launch
import me.ianmooreis.glyph.extensions.config
import me.ianmooreis.glyph.extensions.contentClean
import me.ianmooreis.glyph.extensions.reply
import me.ianmooreis.glyph.orchestrators.DatabaseOrchestrator
import me.ianmooreis.glyph.orchestrators.StatusOrchestrator
import me.ianmooreis.glyph.orchestrators.messaging.quickview.furaffinity.FurAffinity
import me.ianmooreis.glyph.orchestrators.messaging.quickview.picarto.Picarto
import me.ianmooreis.glyph.orchestrators.skills.SkillOrchestrator
import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.entities.Emote
import net.dv8tion.jda.core.entities.Game
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.events.message.MessageDeleteEvent
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import net.jodah.expiringmap.ExpiringMap
import org.apache.commons.codec.digest.DigestUtils
import org.slf4j.Logger
import org.slf4j.simple.SimpleLoggerFactory
import java.util.concurrent.TimeUnit

/**
 * Manages message events including handling incoming messages and dispatching the SkillOrchestrator in addition to the message ledger
 */
object MessagingOrchestrator : ListenerAdapter() {
    private val log: Logger = SimpleLoggerFactory().getLogger(this.javaClass.simpleName)

    private object DialogFlow : AIDataService(AIConfiguration(System.getenv("DIALOGFLOW_TOKEN")))

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
     * Log a failure to send a message
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
        loadCustomEmotes(event.jda.getGuildById(System.getenv("EMOJI_GUILD")))
        if (event.author.isBot or (event.author == event.jda.selfUser) or event.isWebhookMessage) return
        val config = if (event.channelType.isGuild) event.guild.config else DatabaseOrchestrator.getDefaultServerConfig()
        launch {
            if (config.quickview.furaffinityEnabled) {
                FurAffinity.makeQuickviews(event)
            }
            if (config.quickview.picartoEnabled) {
                Picarto.makeQuickviews(event)
            }
        }
        val message: Message = event.message
        if ((!message.isMentioned(event.jda.selfUser) or (message.contentStripped.trim() == message.contentClean)) and event.message.channelType.isGuild) return
        if (event.message.contentClean.isEmpty()) {
            event.message.reply("You have to say something!")
            return
        }
        val sessionId = DigestUtils.md5Hex(event.author.id + event.channel.id)
        val ctx = AIServiceContextBuilder().setSessionId(sessionId).build()
        val ai = DialogFlow.request(AIRequest(event.message.contentClean), ctx)
        if (ai.isError) {
            event.message.reply("It appears DialogFlow is currently unavailable, please try again later!")
            StatusOrchestrator.setPresence(event.jda, OnlineStatus.DO_NOT_DISTURB, Game.watching("temporary outage at DialogFlow"))
            return
        }
        launch {
            SkillOrchestrator.trigger(event, ai)
        }
        totalMessages++
    }

    /**
     * When a message is deleted anywhere
     */
    override fun onMessageDelete(event: MessageDeleteEvent) {
        val messageId = ledger[event.messageIdLong]
        if (messageId != null) {
            event.channel.getMessageById(messageId).queue {
                it.addReaction("❌").queue()
                it.delete().queueAfter(1, TimeUnit.SECONDS)
                ledger.remove(it.idLong)
            }
        }
    }
}


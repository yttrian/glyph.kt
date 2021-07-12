/*
 * MessagingDirector.kt
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

import io.lettuce.core.RedisFuture
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.events.message.MessageDeleteEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import net.dv8tion.jda.api.interactions.commands.OptionType
import org.yttr.glyph.bot.Director
import org.yttr.glyph.bot.ai.AIAgent
import org.yttr.glyph.bot.extensions.contentClean
import org.yttr.glyph.bot.skills.SkillDirector
import org.yttr.glyph.shared.pubsub.redis.RedisAsync
import java.time.Duration
import java.util.concurrent.TimeUnit

/**
 * Manages message events including handling incoming messages
 * and dispatching the SkillDirector in addition to the message ledger
 */
class MessagingDirector(
    private val aiAgent: AIAgent,
    private val redis: RedisAsync,
    private val skillDirector: SkillDirector,
    configure: Config.() -> Unit = {}
) : Director() {
    /**
     * HOCON-like config for the org.yttr.glyph.bot.quickview.messaging director
     */
    data class Config(
        /**
         * How long associated messages should be remembered for DeleteWith functionality
         */
        var volatileTrackingExpiration: Duration = Duration.ofDays(DEFAULT_VOLATILE_TRACKING_EXPIRATION_DAYS)
    )

    private val config = Config().also(configure)
    private val volatileTrackingExpirationSeconds = config.volatileTrackingExpiration.toSeconds()

    /**
     * When ready, register "quick" command
     */
    override fun onReady(event: ReadyEvent) {
        event.jda.retrieveCommands().queue { commands ->
            val quickExists = commands.any { it.name == "quick" }
            if (!quickExists) {
                event.jda.upsertCommand("quick", "Alternate way to invoke Glyph")
                    .addOption(OptionType.STRING, "request", "What do you want Glyph to do?", true)
                    .setDefaultEnabled(true)
                    .queue()
            }
        }
    }

    /**
     * On the "quick" command, process normally
     */
    override fun onSlashCommand(event: SlashCommandEvent) {
        if (event.name == "quick") {
            event.deferReply().queue {
                processMessage(event.asMessageReceivedEvent) { response ->
                    when (response) {
                        is Response.MessageResponse -> it.editOriginal(response.message)
                        is Response.Reaction -> it.editOriginal(response.emoji)
                        else -> it.editOriginal("I don't know how to help with that.")
                    }.queue()
                }
            }
        }
    }

    private val SlashCommandEvent.asMessageReceivedEvent: MessageReceivedEvent
        get() {
            val request = getOption("request")?.asString ?: ""
            val message = FakeSlashedMessage(this, request)
            val messageReceivedEvent = MessageReceivedEvent(jda, responseNumber, message)
            return SlashMessageReceivedEvent(messageReceivedEvent)
        }

    /**
     * When a new message is seen anywhere
     */
    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (!event.isIgnorable) {
            val message: Message = event.message
            processMessage(event) { response ->
                when (response) {
                    is Response.Ephemeral -> message.reply(response.message, ttl = response.ttl)
                    is Response.Volatile -> message.reply(response.message, volatile = true)
                    is Response.Permanent -> message.reply(response.message, volatile = false)
                    is Response.Reaction -> message.addReaction(response.emoji).queue()
                    else -> Unit
                }
            }
        }
    }

    private fun processMessage(event: MessageReceivedEvent, respond: (Response) -> Unit) {
        // Get ready to ask the DialogFlow agent
        val ai = try {
            aiAgent.request(event.message.contentClean, event.contextHash)
        } catch (e: IllegalArgumentException) {
            log.trace("${aiAgent.name} error", e)
            respond(Response.Reaction("⁉"))
            return
        }

        // In the rare circumstance the agent is unavailable or has an issue, warn the user
        if (ai.isError) {
            respond(
                Response.Volatile(
                    "Sorry, due to an issue with ${aiAgent.name} I'm currently unable to interpret your message."
                )
            )
            return
        }

        // Assuming everything else went well, launch the appropriate skill with the event info and ai response
        skillDirector.launch {
            val response = skillDirector.trigger(event, ai)
            respond(response)
            // Increment the total message count for curiosity's sake
            redis.incr(MESSAGE_COUNT_KEY)
        }
    }

    /**
     * When a message is deleted anywhere, remove the invoking message if considered volatile
     */
    override fun onMessageDelete(event: MessageDeleteEvent) {
        val key = VOLATILE_MESSAGE_PREFIX + event.messageId
        redis.get(key).thenAccept { responseId ->
            redis.del(key)
            event.channel.retrieveMessageById(responseId).queue({
                it.addReaction("❌").queue()
                it.delete().queueAfter(1, TimeUnit.SECONDS)
            }, {})
        }
    }

    /**
     * Add a message to the ledger
     *
     * @param invokerId the message id the invoked the response message
     * @param responseId the message id of the response message to the invoking message
     */
    fun trackVolatile(invokerId: String, responseId: String): RedisFuture<String> =
        redis.setex(VOLATILE_MESSAGE_PREFIX + invokerId, volatileTrackingExpirationSeconds, responseId)

    /**
     * Log a failure to send a message, useful so figuring out the if someone complains Glyph won't respond
     *
     * @param channel the channel where the message failed to send
     */
    private fun logSendFailure(channel: TextChannel, exception: Exception) {
        val warningSuffix = if (channel.type.isGuild) " of ${channel.guild}!" else "!"
        log.warn("Failed to send message in $channel$warningSuffix", exception)
    }

    private fun Message.startsWithMention(user: User) =
        leadingMentionRegex.find(this.contentRaw)?.groups?.get(1)?.value == user.id

    private val MessageReceivedEvent.isIgnorable
        get() = author.isBot || // ignore other bots (which means us too, since we're a bot)
                isWebhookMessage || // ignore webhooks
                message.contentClean.isEmpty() || // ignore empty messages
                (isFromGuild && !message.startsWithMention(jda.selfUser)) // must start with mention of us

    private fun Message.reply(
        message: Message,
        ttl: Duration? = null,
        volatile: Boolean = true
    ) {
        // try to send the message
        try {
            if (isFromGuild) {
                reply(message).mentionRepliedUser(false)
            } else {
                channel.sendMessage(message)
            }.queue {
                if (ttl != null) {
                    it.delete().queueAfter(ttl.seconds, TimeUnit.SECONDS)
                } else if (volatile) {
                    trackVolatile(this.id, it.id)
                }
            }
        } catch (e: InsufficientPermissionException) {
            logSendFailure(textChannel, e)
        }
    }

    companion object {
        /**
         * By default how long to track volatile messages for
         */
        const val DEFAULT_VOLATILE_TRACKING_EXPIRATION_DAYS: Long = 14

        /**
         * Number of messages Glyph has triggered a skill for since we started tracking
         */
        const val MESSAGE_COUNT_KEY: String = "Glyph:Messages:Count"

        private const val VOLATILE_MESSAGE_PREFIX: String = "Glyph:Message:Volatile:"

        /**
         * Grabs the mention at the start of a message, if any
         */
        private val leadingMentionRegex: Regex = Regex("^<@!?(\\d+)>")
    }
}

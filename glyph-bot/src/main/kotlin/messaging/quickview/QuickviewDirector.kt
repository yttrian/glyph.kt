/*
 * QuickviewDirector.kt
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

package org.yttr.glyph.bot.messaging.quickview

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.MessageAction
import org.yttr.glyph.bot.Director
import org.yttr.glyph.bot.messaging.MessagingDirector
import org.yttr.glyph.bot.messaging.quickview.furaffinity.FurAffinityGenerator
import org.yttr.glyph.bot.messaging.quickview.picarto.PicartoGenerator
import java.time.Duration

/**
 * Handle triggers for quickviews
 */
class QuickviewDirector(private val messagingDirector: MessagingDirector) : Director() {
    companion object {
        /**
         * The maximum amount of time the generators are allowed to process a message for
         */
        private const val GENERATOR_TIMEOUT_SECONDS: Long = 15

        /**
         * Maximum number of embeds that should be generated
         */
        private const val EMBED_LIMIT: Int = 5
    }

    private val generators = setOf(PicartoGenerator(), FurAffinityGenerator())
    private val generatorTimeout = Duration.ofSeconds(GENERATOR_TIMEOUT_SECONDS).toMillis()

    private data class EmbedFold(val replyMessage: Message? = null, val embeds: List<MessageEmbed> = emptyList())

    /**
     * Check for quickviews when a message is received
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.isIgnorable) return
        val config = if (event.channelType.isGuild) event.guild.config else configDirector.defaultConfig

        launch {
            withTimeout(generatorTimeout) {
                generators.map { it.generate(event, config.quickview) }.merge().take(EMBED_LIMIT)
                    .fold(EmbedFold()) { (message, embeds), newEmbed ->
                        when {
                            message == null -> {
                                event.message.suppressEmbeds(true).reason("Quickview").queue({}) {
                                    log.debug(
                                        "Unable to suppress embeds for Quickviews in context ${event.contextHash}"
                                    )
                                }
                                val replyMessage = event.message.replyEmbeds(newEmbed)
                                    .mentionRepliedUser(false)
                                    .await()
                                messagingDirector.trackVolatile(event.messageId, replyMessage.id)
                                EmbedFold(replyMessage, listOf(newEmbed))
                            }
                            !embeds.contains(newEmbed) -> {
                                val newEmbeds = embeds.plus(newEmbed)
                                message.editMessageEmbeds(newEmbeds).queue()
                                EmbedFold(message, newEmbeds)
                            }
                            else -> {
                                EmbedFold(message, embeds)
                            }
                        }
                    }
            }
        }
    }

    private suspend fun MessageAction.await() = this.submit().await()

    private val MessageReceivedEvent.isIgnorable
        get() = author.isBot || isWebhookMessage || (author == jda.selfUser)
}

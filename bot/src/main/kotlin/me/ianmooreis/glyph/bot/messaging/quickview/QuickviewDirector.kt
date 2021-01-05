/*
 * QuickviewDirector.kt
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

package me.ianmooreis.glyph.bot.messaging.quickview

import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import me.ianmooreis.glyph.bot.Director
import me.ianmooreis.glyph.bot.messaging.MessagingDirector
import me.ianmooreis.glyph.bot.messaging.quickview.furaffinity.FurAffinityGenerator
import me.ianmooreis.glyph.bot.messaging.quickview.picarto.PicartoGenerator
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.MessageAction
import java.time.Duration

/**
 * Handle triggers for quickviews
 */
class QuickviewDirector(private val messagingDirector: MessagingDirector) : Director() {
    companion object {
        /**
         * The maximum amount of time the generators are allowed to process a message for
         */
        const val GENERATOR_TIMEOUT_SECONDS: Long = 15
    }

    private val generators = setOf(PicartoGenerator(), FurAffinityGenerator())
    private val generatorTimeout = Duration.ofSeconds(GENERATOR_TIMEOUT_SECONDS).toMillis()

    /**
     * Check for quickviews when a message is received
     */
    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.isIgnorable) return
        val config = if (event.channelType.isGuild) event.guild.config else configDirector.defaultConfig

        launch {
            withTimeout(generatorTimeout) {
                generators.forEach {
                    it.generate(event, config.quickview).fold(event.messageId) { messageId, embed ->
                        if (messageId == event.messageId) {
                            event.message.suppressEmbeds(true).reason("Quickview").queue({}) {
                                log.debug("Unable to suppress embeds for Quickviews in context ${event.contextHash}")
                            }
                        }
                        val responseId = event.channel.sendMessage(embed).await().id
                        messagingDirector.trackVolatile(messageId, responseId)
                        responseId
                    }
                }
            }
        }
    }

    private suspend fun MessageAction.await() = this.submit().await()

    private val MessageReceivedEvent.isIgnorable
        get() = author.isBot || isWebhookMessage || (author == jda.selfUser)
}

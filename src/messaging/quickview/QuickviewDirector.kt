/*
 * QuickviewDirector *
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

package me.ianmooreis.glyph.messaging.quickview

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import me.ianmooreis.glyph.Director
import me.ianmooreis.glyph.directors.config.ConfigDirector
import me.ianmooreis.glyph.extensions.config
import me.ianmooreis.glyph.messaging.MessagingDirector
import me.ianmooreis.glyph.messaging.quickview.furaffinity.FurAffinityGenerator
import me.ianmooreis.glyph.messaging.quickview.picarto.PicartoGenerator
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
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
        val config = if (event.channelType.isGuild) event.guild.config else ConfigDirector.getDefaultServerConfig()

        launch {
            withTimeout(generatorTimeout) {
                generators.forEach {
                    it.generate(event, config.quickview).collect { embed ->
                        event.channel.sendMessage(embed).queue { response ->
                            messagingDirector.trackVolatile(event.messageId, response.id)
                        }
                    }
                }
            }
        }
    }

    private val MessageReceivedEvent.isIgnorable
        get() = author.isBot || isWebhookMessage || (author == jda.selfUser)
}

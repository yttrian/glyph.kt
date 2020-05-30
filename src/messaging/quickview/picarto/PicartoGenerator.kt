/*
 * Picarto.kt
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

package me.ianmooreis.glyph.messaging.quickview.picarto

import io.ktor.client.features.ResponseException
import io.ktor.client.request.get
import io.ktor.http.takeFrom
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.mapNotNull
import me.ianmooreis.glyph.database.config.server.QuickviewConfig
import me.ianmooreis.glyph.messaging.quickview.QuickviewGenerator
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

/**
 * Handles the creation of QuickViews for picarto.tv links
 */
class PicartoGenerator : QuickviewGenerator() {
    companion object {
        private const val API_BASE: String = "https://api.picarto.tv"
    }

    private val urlFormat = Regex("(?:picarto.tv)/(\\w*)", RegexOption.IGNORE_CASE)

    override suspend fun generate(event: MessageReceivedEvent, config: QuickviewConfig): Flow<MessageEmbed> =
        if (config.picartoEnabled) findChannelNames(event.message.contentRaw).mapNotNull {
            getChannel(it)?.getEmbed()
        } else emptyFlow()

    /**
     * Attempt to find Picarto channel names from links in a message, if any
     */
    fun findChannelNames(content: String): Flow<String> =
        urlFormat.findAll(content).asFlow().mapNotNull { it.groups[1]?.value }

    private suspend fun getChannel(name: String): Channel? = try {
        client.get<Channel> {
            url.takeFrom(API_BASE).path("v1", "channel", "name", name)
        }
    } catch (e: ResponseException) {
        null
    }
}

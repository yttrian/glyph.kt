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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.mapNotNull
import me.ianmooreis.glyph.directors.config.server.QuickviewConfig
import me.ianmooreis.glyph.extensions.contentClean
import me.ianmooreis.glyph.messaging.quickview.QuickviewGenerator
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

/**
 * Handles the creation of QuickViews for picarto.tv links
 */
class PicartoGenerator : QuickviewGenerator() {
    private val urlFormat = Regex("((http[s]?)://)?(www.)?(picarto.tv)/(\\w*)/?", RegexOption.IGNORE_CASE)

    override suspend fun generate(event: MessageReceivedEvent, config: QuickviewConfig): Flow<MessageEmbed> {
        return if (config.picartoEnabled) {
            urlFormat.findAll(event.message.contentClean).asFlow()
                .mapNotNull { getChannel(it.groups[5]?.value)?.getEmbed() }
        } else emptyFlow()
    }

    private suspend fun getChannel(name: String?): Channel? = try {
        client.get<Channel>("https://api.picarto.tv/v1/channel/name/$name")
    } catch (e: ResponseException) {
        null
    }
}
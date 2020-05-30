/*
 * QuickviewGenerator.kt
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

package me.ianmooreis.glyph.messaging.quickview

import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import kotlinx.coroutines.flow.Flow
import me.ianmooreis.glyph.database.config.server.QuickviewConfig
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.io.Closeable

/**
 * Handle extract data from websites to build relevant QuickViews
 */
abstract class QuickviewGenerator : Closeable {
    /**
     * HTTP client for making API requests
     */
    protected val client: HttpClient = HttpClient {
        install(JsonFeature)
    }

    /**
     * Generate QuickView embeds for any links found in the message
     */
    abstract suspend fun generate(event: MessageReceivedEvent, config: QuickviewConfig): Flow<MessageEmbed>

    /**
     * Closes the client used by the generator
     */
    override fun close(): Unit = client.close()
}

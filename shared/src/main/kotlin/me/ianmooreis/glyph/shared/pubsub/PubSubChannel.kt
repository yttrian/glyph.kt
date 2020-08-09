/*
 * PubSubChannels.kt
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

package me.ianmooreis.glyph.shared.pubsub

/**
 * Channel names and prefixes for PubSub
 */
enum class PubSubChannel(
    /**
     * The value associated with the enum item
     */
    val value: String
) {
    /**
     * Send a server ID to this channel to refresh the cached config for it
     */
    CONFIG_REFRESH("Glyph:Config:Refresh"),

    /**
     * Used for ask and responder
     */
    CONFIG_PREFIX("Glyph:Config")
}

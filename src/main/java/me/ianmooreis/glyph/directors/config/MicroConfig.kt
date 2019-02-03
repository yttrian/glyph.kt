/*
 * MicroConfig.kt
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

package me.ianmooreis.glyph.directors.config

import net.dv8tion.jda.core.entities.Guild

/**
 * A config format designed to minimize size, not intended to be read by humans (but can be)
 */
class MicroConfig internal constructor(
    /**
     * A list of the micro-config values
     */
    private val values: List<String>
) {
    /**
     * The max length of the
     */
    val size: Int = values.size

    override fun toString(): String {
        return values.joinToString("\n")
    }

    /**
     * Get a string from a line of the micro-config
     */
    fun getString(line: Int): String {
        return values[line]
    }

    /**
     * Get an int from a line of the micro-config
     */
    fun getInt(line: Int): Int {
        return values[line].toInt(36)
    }

    /**
     * Get and unpack a boolean group from a line of the micro-config
     */
    fun getBooleans(line: Int): List<Boolean> {
        return values[line].toInt(2).toString().map {
            it.toString().toBoolean()
        }
    }

    /**
     * Get a webhook URL from a line of the micro-config
     */
    fun getWebhook(line: Int, guild: Guild): String {
        // TODO: Avoid blocking
        val webhooks = guild.webhooks.complete()
        val webhookId = values[line].toLong(36)

        val url = webhooks.find {
            it.idLong == webhookId
        }?.url

        if (url === null) {
            throw MicroConfigException("Provided webhook could not be found in server!")
        }

        return url
    }
}
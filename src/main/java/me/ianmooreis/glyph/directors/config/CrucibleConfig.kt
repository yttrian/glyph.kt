/*
 * CrucibleConfig.kt
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
 * A configuration for auto moderation
 */
data class CrucibleConfig(
    /**
     * Ban joining members that have URLs in their name
     */
    var banURLsInNames: Boolean = false
) : Config {
    override fun dumpMicroConfig(guild: Guild): MicroConfig {
        return MicroConfigBuilder()
            .addValue(banURLsInNames)
            .build()
    }

    override fun loadMicroConfig(guild: Guild, microConfig: MicroConfig) {
        val booleans = microConfig.getBooleans(0)
        banURLsInNames = booleans[0]
    }
}
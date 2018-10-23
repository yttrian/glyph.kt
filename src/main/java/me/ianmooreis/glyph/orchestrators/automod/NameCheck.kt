/*
 * NameCheck.kt
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

package me.ianmooreis.glyph.orchestrators.automod

import me.ianmooreis.glyph.extensions.config
import net.dv8tion.jda.core.entities.Member

/**
 * Handles the checking of names for auto moderation
 */
object NameCheck {
    /**
     * Checks if a name is "illegal"
     *
     * @param member the member to check
     *
     * @return whether or not their name is "illegal"
     */
    fun isIllegal(member: Member): Boolean {
        val config = member.guild.config
        val banURLsInNames = config.crucible.banURLsInNames
        return (containsURL(member.effectiveName) && banURLsInNames)
    }

    /**
     * Reports whether or not a name contains a URL
     *
     * @param name the name to check
     *
     * @return whether or not a url was found in the name
     */
    private fun containsURL(name: String): Boolean {
        // We have to be careful to not be too aggressive and complain about people
        // We also don't want to "test" urls by trying to visit them because that could be a very bad idea
        // We'll be a bit lax as to not be overaggressive and require the URL to be lowercase and have a TLD of 2-3 chars
        // Typically twitter.com, discord.gg, and discord.me are the most popular spam bot names
        val urlRegex = Regex("((http|https)://)?(([\\w.-]*)\\.([\\w]{2,3}))", RegexOption.IGNORE_CASE)
        val rawMatches = urlRegex.findAll(name)
        val validMatches = rawMatches.toList().filter { result ->
            result.value.toLowerCase() == result.value
        }
        return validMatches.isNotEmpty()
    }
}
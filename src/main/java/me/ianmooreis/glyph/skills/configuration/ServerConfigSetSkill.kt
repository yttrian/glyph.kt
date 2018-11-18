/*
 * ServerConfigSetSkill.kt
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

package me.ianmooreis.glyph.skills.configuration

import ai.api.model.AIResponse
import me.ianmooreis.glyph.directors.skills.Skill
import me.ianmooreis.glyph.extensions.reply
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.jodah.expiringmap.ExpirationPolicy
import net.jodah.expiringmap.ExpiringMap
import java.awt.Color
import java.time.Instant
import java.util.concurrent.TimeUnit

/**
 * A skill to set a server from a Hastebin url
 */
object ServerConfigSetSkill : Skill("skill.configuration.load", cooldownTime = 15, guildOnly = true, requiredPermissionsUser = listOf(Permission.ADMINISTRATOR)) {
    private val strugglers: MutableMap<Member, Int> = ExpiringMap.builder().expiration(10, TimeUnit.MINUTES).expirationPolicy(ExpirationPolicy.ACCESSED).build()

    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        event.message.reply("well, this ain't it chief!")
    }

    private fun updateSuccess(event: MessageReceivedEvent) {
        event.message.reply(EmbedBuilder()
            .setTitle("Configuration Updated")
            .setDescription("The server configuration has been successfully updated!")
            .setFooter("Configuration", null)
            .setTimestamp(Instant.now())
            .build())
    }

    private fun updateError(event: MessageReceivedEvent, struggling: Boolean) {
        event.message.reply(EmbedBuilder()
            .setTitle("Configuration Update Error")
            .setDescription(
                if (struggling) {
                    "**You appear to be struggling. Be sure to check out the help links below, " +
                        "or speak to someone in the [official Glyph server](https://discord.me/glyph-discord).**"
                } else {
                    "This servers configuration failed to update!"
                } + "\n\n" +
                    "Make sure your YAML is in the correct format and that it follows the rules in the documentation. " +
                    "The most common error is forgetting to indent.\n\n" +
                    "[Documentation](https://glyph-discord.readthedocs.io/en/latest/configuration.html) - " +
                    "[Official Glyph Server](https://discord.me/glyph-discord) - " +
                    "[YAML Format Checker](https://yaml-online-parser.appspot.com/)")
            .setColor(Color.RED)
            .setFooter("Configuration", null)
            .setTimestamp(Instant.now())
            .build())
    }
}
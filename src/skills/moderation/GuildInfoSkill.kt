/*
 * GuildInfoSkill.kt
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

package me.ianmooreis.glyph.skills.moderation

import me.ianmooreis.glyph.directors.messaging.AIResponse
import me.ianmooreis.glyph.directors.skills.Skill
import me.ianmooreis.glyph.extensions.asPlainMention
import me.ianmooreis.glyph.extensions.getInfoEmbed
import me.ianmooreis.glyph.extensions.isBotFarm
import me.ianmooreis.glyph.extensions.reply
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.ocpsoft.prettytime.PrettyTime

/**
 * A skill that allows users to ask for different info about a guild
 */
object GuildInfoSkill : Skill("skill.moderation.guildInfo", guildOnly = true) {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        val property: String? = ai.result.getStringParameter("guildProperty")
        if (property != null) {
            val guild = event.guild
            event.message.reply(
                when (property) {
                    "name" -> "This guild is **${guild.name}**."
                    "id" -> "The id for ${guild.name} is **${guild.id}**."
                    "region" -> "${guild.name} is located in **${guild.regionRaw}**."
                    "created" -> "${guild.name} was created **${PrettyTime().format(guild.creationTime.toDate())}** (${guild.creationTime})."
                    "owner" -> "**${guild.owner.asPlainMention}** is the owner of ${guild.name}."
                    "members" -> "${guild.name} has **${guild.members.count()}** members."
                    "membersHumans" -> "${guild.name} has **${guild.members.count { !it.user.isBot }}** humans."
                    "membersBots" -> "${guild.name} has **${guild.members.count { it.user.isBot }}** bots."
                    "channels" -> "${guild.name} has **${guild.textChannels.size + guild.voiceChannels.size}** channels."
                    "channelsText" -> "${guild.name} has **${guild.textChannels.size}** text channels."
                    "channelsVoice" -> "${guild.name} has **${guild.voiceChannels.size}** voice channels."
                    "roles" -> "${guild.name} has **${guild.roles.size}** roles."
                    "farm" -> if (guild.isBotFarm) "**Yes**, ${guild.name} is a bot farm!" else "**No**, ${guild.name} is not a bot farm."
                    else -> "I'm not sure what property `$property` is for a guild."
                }
            )
        } else {
            event.message.reply(event.guild.getInfoEmbed("Guild Info", "Moderation", null, true))
        }
    }
}
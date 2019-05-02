/*
 * RoleListSkill.kt
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

package me.ianmooreis.glyph.skills.roles

import ai.api.model.AIResponse
import me.ianmooreis.glyph.directors.skills.Skill
import me.ianmooreis.glyph.extensions.config
import me.ianmooreis.glyph.extensions.random
import me.ianmooreis.glyph.extensions.reply
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.time.Instant

/**
 * A skill that allows members to list all selectable roles
 */
object RoleListSkill : Skill("skill.role.list", guildOnly = true) {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        val selectableRoles = event.guild.config.selectableRoles.roles.mapNotNull { event.guild.getRoleById(it) }
        val limit = event.guild.config.selectableRoles.limit
        if (selectableRoles.isNotEmpty()) {
            val randomRole = selectableRoles.random()
            event.message.reply(EmbedBuilder()
                .setTitle("Available Roles")
                .setDescription(selectableRoles.joinToString("\n") {
                    val size = it.guild.getMembersWithRoles(it).size
                    "**${it.name}** $size ${if (size == 1) "member" else "members"}"
                } + if (limit > 0) "\n*You can have up to $limit ${if (limit == 1) "role" else "roles"}*" else "")
                .setFooter(
                    "Roles ${if (randomRole != null) "| Try asking \"Set me as ${randomRole.name}\"" else ""}",
                    null
                )
                .setTimestamp(Instant.now())
                .build())
        } else {
            event.message.reply("There are no selectable roles configured!")
        }
    }
}
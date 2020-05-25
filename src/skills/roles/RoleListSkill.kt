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

import me.ianmooreis.glyph.ai.AIResponse
import me.ianmooreis.glyph.directors.skills.Skill
import me.ianmooreis.glyph.extensions.config
import me.ianmooreis.glyph.messaging.Response
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.time.Instant

/**
 * A skill that allows members to list all selectable roles
 */
class RoleListSkill : Skill("skill.role.list", guildOnly = true) {
    override suspend fun onTrigger(event: MessageReceivedEvent, ai: AIResponse): Response {
        val selectableRoles = event.guild.config.selectableRoles.roles.mapNotNull { event.guild.getRoleById(it) }
        val limit = event.guild.config.selectableRoles.limit

        return if (selectableRoles.isNotEmpty()) {
            val randomRole = selectableRoles.random()
            val description = StringBuilder()

            selectableRoles.forEach {
                description.appendln(it.asMention)
            }
            if (limit > 0) description.append("*You can have up to $limit ${if (limit == 1) "role" else "roles"}*")

            Response.Volatile(
                EmbedBuilder()
                    .setTitle("Available Roles")
                    .setDescription(description)
                    .setFooter("Roles | Try asking \"Set me as ${randomRole.name}\"}")
                    .setTimestamp(Instant.now())
                    .build()
            )
        } else {
            Response.Volatile("There are no selectable roles configured!")
        }
    }
}
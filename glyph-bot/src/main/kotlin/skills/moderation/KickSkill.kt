/*
 * KickSkill.kt
 *
 * Glyph, a Discord bot that uses natural language instead of commands
 * powered by DialogFlow and Kotlin
 *
 * Copyright (C) 2017-2021 by Ian Moore
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

package org.yttr.glyph.bot.skills.moderation

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.yttr.glyph.bot.ai.AIResponse
import org.yttr.glyph.bot.directors.AuditingDirector.audit
import org.yttr.glyph.bot.directors.messaging.SimpleDescriptionBuilder
import org.yttr.glyph.bot.extensions.asPlainMention
import org.yttr.glyph.bot.extensions.sendDeathPM
import org.yttr.glyph.bot.messaging.Response
import org.yttr.glyph.bot.skills.Skill

/**
 * A skill that allows privileged members to kick other members
 */
class KickSkill : Skill(
    "skill.moderation.kick",
    guildOnly = true,
    requiredPermissionsSelf = listOf(Permission.KICK_MEMBERS),
    requiredPermissionsUser = listOf(Permission.KICK_MEMBERS)
) {
    override suspend fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) =
        KickBanSkillHelper.getInstance(event, ai, "kick") { targets, reason ->
            event.message.delete().reason("Kick request").queue()
            targets.forEach { member ->
                member.user.sendDeathPM("***You have been kicked from ${event.guild.name} for \"$reason\"!***") {
                    event.guild.kick(member, reason).queue()
                }
            }
            val targetNames = targets.joinToString { it.asPlainMention }
            if (event.guild.config.auditing.kicks) {
                val auditMessage = SimpleDescriptionBuilder()
                    .addField("Who", if (targetNames.length < 200) targetNames else "${targets.size} people")
                    .addField("Blame", event.author.asMention)
                    .addField("Reason", reason)
                    .build()
                event.guild.audit("Members Kicked", auditMessage)
            }

            Response.Permanent("***${if (targetNames.length < 200) targetNames else "${targets.size} people"} ${if (targets.size == 1) "was" else "were"} kicked!***")
        }
}

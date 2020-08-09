/*
 * RoleUnsetSkill.kt
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

package me.ianmooreis.glyph.bot.skills.roles

import me.ianmooreis.glyph.bot.ai.AIResponse
import me.ianmooreis.glyph.bot.directors.skills.Skill
import me.ianmooreis.glyph.bot.extensions.asPlainMention
import me.ianmooreis.glyph.bot.extensions.cleanMentionedMembers
import me.ianmooreis.glyph.bot.messaging.Response
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.exceptions.HierarchyException

/**
 * A skill to allow members to unset a selectable role
 */
class RoleUnsetSkill : Skill(
    "skill.role.unset",
    guildOnly = true,
    requiredPermissionsSelf = listOf(Permission.MANAGE_ROLES)
) {
    override suspend fun onTrigger(event: MessageReceivedEvent, ai: AIResponse): Response {
        //Check if the user is allowed to remove roles for the specified target(s)
        if ((event.message.cleanMentionedMembers.isNotEmpty() || event.message.mentionsEveryone()) && !event.member!!.hasPermission(
                listOf(Permission.MANAGE_ROLES)
            )
        ) {
            return Response.Volatile("You must have Manage Roles permission to remove other peoples' roles!")
        }

        return RoleSkillHelper.getInstance(event, ai, event.guild.config.selectableRoles) { desiredRole, _, targets ->
            //Remove the role
            targets.forEach {
                try {
                    event.guild.removeRoleFromMember(it, desiredRole)
                        .reason("Asked to not be $desiredRole.Name").queue()
                } catch (e: HierarchyException) {
                    this.log.debug("Can not remove role ${desiredRole.name} from members in ${event.guild}")
                }
            }
            val targetNames = targets.joinToString { it.asPlainMention }
            Response.Volatile("*${if (targetNames.length < 50) targetNames else "${targets.size} people"} ${if (targets.size == 1) "is" else "are"} no longer ${desiredRole.name}!*")
        }
    }
}
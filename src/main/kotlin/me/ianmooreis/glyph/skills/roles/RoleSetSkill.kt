/*
 * RoleSetSkill.kt
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
import me.ianmooreis.glyph.directors.messaging.CustomEmote
import me.ianmooreis.glyph.directors.skills.Skill
import me.ianmooreis.glyph.extensions.*
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.exceptions.HierarchyException
import java.util.concurrent.TimeUnit

/**
 * A skill that allows members to assign selectable roles
 */
object RoleSetSkill :
    Skill("skill.role.set", guildOnly = true, requiredPermissionsSelf = listOf(Permission.MANAGE_ROLES)) {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        //Check if the user is allowed to set roles for the specified target(s)
        if ((event.message.cleanMentionedMembers.isNotEmpty() || event.message.mentionsEveryone()) && !event.member.hasPermission(
                Permission.MANAGE_ROLES
            )
        ) {
            event.message.reply("You must have Manage Roles permission to set other peoples' roles!")
            return
        }
        RoleSkillHelper.getInstance(event, ai) { desiredRole, selectableRoles, targets ->
            val config = event.guild.config.selectableRoles
            //If the user is the only target and does not have manage roles permission and would violate the limit, make them remove a role first (mods can ignore this)
            if (targets.size > 1 && targets.contains(event.member) && !event.member.hasPermission(Permission.MANAGE_ROLES)
                && event.member.roles.count { selectableRoles.contains(it) } >= config.limit
            ) {
                val randomRole = event.member.roles.filter { selectableRoles.contains(it) }.random()
                event.message.reply(
                    "" +
                        "${CustomEmote.XMARK} You can only have ${config.limit} roles in this server! " +
                        (if (randomRole != null) "Try removing one first, by telling me for example: \"remove me from ${randomRole.name}\"" else "")
                )
            } else {
                //Remove old roles if the sever role limit is 1, this is the default and is meant for switching roles
                if (config.limit == 1) {
                    targets.forEach {
                        try {
                            event.guild.controller.removeRolesFromMember(it, selectableRoles)
                                .reason("Asked to be ${desiredRole.name}").queue()
                        } catch (e: IllegalArgumentException) {
                            this.log.debug("No roles needed to be removed from $it in ${event.guild}")
                        } catch (e: HierarchyException) {
                            this.log.debug("Can not remove role ${desiredRole.name} from members in ${event.guild}")
                        }
                    }
                }
                //Grant everyone the desired role but report a warning if the role is too high
                try {
                    targets.forEach { target ->
                        event.guild.controller.addSingleRoleToMember(target, desiredRole)
                            .reason("Asked to be ${desiredRole.name}").queueAfter(500, TimeUnit.MILLISECONDS)
                    }
                    val targetNames = targets.joinToString { it.asPlainMention }
                    event.message.reply("*${if (targetNames.length < 50) targetNames else "${targets.size} people"} ${if (targets.size == 1) "is" else "are"} now ${desiredRole.name}!*")
                } catch (e: HierarchyException) {
                    event.message.reply("${CustomEmote.XMARK} I can not set anyone as `${desiredRole.name}` because it is above my highest role!")
                }
            }
        }
    }
}
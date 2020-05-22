/*
 * KickBanSkillHelper.kt
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

import me.ianmooreis.glyph.ai.AIResponse
import me.ianmooreis.glyph.extensions.cleanMentionedMembers
import me.ianmooreis.glyph.messaging.Response
import me.ianmooreis.glyph.messaging.VolatileResponse
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

/**
 * Helps run all the checks before allowing a member to kick or ban
 */
object KickBanSkillHelper {
    /**
     * Run checks and created a list of targets to kick or ban if allowed
     *
     * @param event the message event
     * @param ai the ai response
     * @param action the action the user is trying to perform (kick or ban)
     * @param success the call back to run if the targets are found and allowed
     */
    fun getInstance(
        event: MessageReceivedEvent,
        ai: AIResponse,
        action: String,
        success: (targets: List<Member>, reason: String) -> Response
    ): Response {
        val authorMaxRole = maxRolePosition(event.member!!)
        val selfMaxRole = maxRolePosition(event.guild.selfMember)
        val targets = event.message.cleanMentionedMembers
        return when {
            event.message.mentionsEveryone() -> VolatileResponse("You cannot $action everyone at once!")
            targets.isEmpty() -> VolatileResponse("You need to @mention at least one member to $action!")
            targets.contains(event.member!!) -> VolatileResponse("You cannot $action yourself!")
            targets.contains(event.guild.owner) -> VolatileResponse("You cannot $action the owner!")
            targets.filterNot { it.hasPermission(Permission.ADMINISTRATOR) }
                .isEmpty() -> VolatileResponse("I will not $action someone with Administrator permissions!")
            targets.filterNot { it.hasPermission(Permission.MANAGE_SERVER) }
                .isEmpty() -> VolatileResponse("I will not $action someone with Manage Server permissions!")
            targets.contains(event.guild.selfMember) -> VolatileResponse(
                "I cannot kick myself!"
            )
            event.message.cleanMentionedMembers.filterNot { maxRolePosition(it) >= authorMaxRole }
                .isEmpty() && !event.member!!.isOwner -> VolatileResponse(
                "You cannot $action members of your role or higher!"
            )
            event.message.cleanMentionedMembers.filterNot { maxRolePosition(it) >= selfMaxRole }
                .isEmpty() -> VolatileResponse(
                "I cannot $action members of my role or higher!"
            )
            else -> success(
                targets,
                ai.result.getStringParameter("reason") ?: "No reason provided"
            )
        }
    }

    private fun maxRolePosition(member: Member): Int {
        return member.roles.map { it.position }.max() ?: 0
    }
}
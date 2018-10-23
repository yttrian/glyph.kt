/*
 * CrucibleOrchestratorr.kt
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

import me.ianmooreis.glyph.extensions.asPlainMention
import me.ianmooreis.glyph.extensions.audit
import me.ianmooreis.glyph.extensions.sendDeathPM
import me.ianmooreis.glyph.orchestrators.messaging.CustomEmote
import me.ianmooreis.glyph.orchestrators.messaging.SimpleDescriptionBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import org.slf4j.Logger
import org.slf4j.simple.SimpleLoggerFactory
import java.awt.Color

/**
 * Manages auto moderation actions, if enabled via server config
 */
object CrucibleOrchestrator : ListenerAdapter() {
    private val log: Logger = SimpleLoggerFactory().getLogger(this.javaClass.simpleName)

    /**
     * Perform auto moderator checks when someone joins a server
     */
    override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        val member = event.member
        if (NameCheck.isIllegal(member)) {
            ban(member, "illegal name")
        }
    }

    private fun ban(member: Member, reason: String) {
        val guild = member.guild
        val description = SimpleDescriptionBuilder()
            .addField("Who", member.asPlainMention)
            .addField("Reason", reason)
            .build()
        if (guild.selfMember.hasPermission(Permission.BAN_MEMBERS)) {
            member.user.sendDeathPM("***${CustomEmote.GRIMACE} You have been automatically banned from ${member.guild.name} for \"$reason\"!***") {
                guild.controller.ban(member, 7, reason).queue {
                    member.guild.audit("Crucible Ban", description, Color.RED)
                }
            }
        } else {
            guild.audit("Crucible Warning", description, Color.YELLOW)
        }
    }
}
/*
 * AuditingDirector.kt
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

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent
import net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent
import org.yttr.glyph.bot.Director
import org.yttr.glyph.bot.directors.messaging.SimpleDescriptionBuilder
import org.yttr.glyph.bot.extensions.getInfoEmbed
import org.yttr.glyph.bot.messaging.WebhookDirector
import java.awt.Color
import java.time.Instant

/**
 * Manages auditing logs for servers
 */
object AuditingDirector : Director() {

    /**
     * When a member joins a guild
     */
    override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        if (event.guild.config.auditing.joins) {
            val embed = event.user.getInfoEmbed("Member Joined", "Auditing", Color.GREEN)
            event.guild.audit(embed.title!!, embed.description!!, embed.color)
        }
    }

    /**
     * When a member leaves a guild
     */
    override fun onGuildMemberRemove(event: GuildMemberRemoveEvent) {
        if (event.guild.config.auditing.leaves) {
            val embed = event.user.getInfoEmbed("Member Left", "Auditing", Color.RED)
            event.guild.audit(embed.title!!, embed.description!!, embed.color)
        }
    }

    /**
     * When messages are bulk deleted
     */
    override fun onMessageBulkDelete(event: MessageBulkDeleteEvent) {
        if (event.guild.config.auditing.purge) {
            event.guild.audit(
                "Purge",
                "${event.messageIds.size} messages deleted in ${event.channel.asMention}",
                Color.YELLOW
            )
        }
    }

    /**
     * When a user updates their name
     */
    override fun onUserUpdateName(event: UserUpdateNameEvent) {
        val description = SimpleDescriptionBuilder()
            .addField("Old", event.oldName)
            .addField("New", event.newName)
            .addField("Mention", event.user.asMention)
            .addField("ID", event.user.id)
            .build()
        event.user.mutualGuilds.filter { it.config.auditing.names }.forEach { guild ->
            guild.audit("Name Change", description, Color.orange)
        }
    }

    /**
     * Sends an audit embed to a guild's auditing webhook (if it has one)
     *
     * @param title the title of the embed
     * @param description the body of the embed
     * @param color the color of the embed
     */
    fun Guild.audit(title: String, description: String, color: Color? = null) {
        val channelID = this.config.auditing.channel
        if (channelID !== null) {
            val channel = this.getTextChannelById(channelID) // channel must belong to server
            if (channel !== null) {
                WebhookDirector.send(
                    channel,
                    EmbedBuilder()
                        .setTitle(title)
                        .setDescription(description)
                        .setFooter("Auditing", null)
                        .setColor(color)
                        .setTimestamp(Instant.now())
                        .build()
                )
            }
        }
    }
}

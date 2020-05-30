/*
 * ServerConfigSkill
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

import kotlinx.coroutines.launch
import me.ianmooreis.glyph.Director
import me.ianmooreis.glyph.ai.AIResponse
import me.ianmooreis.glyph.database.config.ConfigDirector
import me.ianmooreis.glyph.database.config.server.AuditingConfig
import me.ianmooreis.glyph.database.config.server.QuickviewConfig
import me.ianmooreis.glyph.database.config.server.SelectableRolesConfig
import me.ianmooreis.glyph.database.config.server.ServerConfig
import me.ianmooreis.glyph.database.config.server.StarboardConfig
import me.ianmooreis.glyph.database.config.server.WikiConfig
import me.ianmooreis.glyph.directors.skills.Skill
import me.ianmooreis.glyph.extensions.config
import me.ianmooreis.glyph.messaging.Response
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent

/**
 * The skill for getting a server configuration which will be posted to Hastebin in YAML format
 */
class ServerConfigSkill : Skill(
    "skill.config.server",
    cooldownTime = 15,
    guildOnly = true,
    requiredPermissionsSelf = listOf(Permission.MANAGE_WEBHOOKS),
    requiredPermissionsUser = listOf(Permission.ADMINISTRATOR)
) {
    override suspend fun onTrigger(event: MessageReceivedEvent, ai: AIResponse): Response {
        return when (ai.result.getStringParameter("action")) {
            "get" -> {
                val micro = toMicro(event.guild).build()

                val message = "Use the link below to edit your config. " +
                    "When done, click Save and Copy Key, send me a direct message with the key you got.\n" +
                    "https://gl.yttr.org/config#$micro"

                if (micro.length > Message.MAX_CONTENT_LENGTH) {
                    Response.Volatile("I'm sorry, your config cannot be edited for inane reasons!")
                } else {
                    val guild = event.guild
                    val configListener = object : Director() {
                        override fun onPrivateMessageReceived(event: PrivateMessageReceivedEvent) {
                            launch {
                                guild.config = fromMicro(MicroConfig.Reader().read(event.message.contentRaw))
                                event.message.addReaction("✔").queue()
                            }
                            event.jda.removeEventListener(this)
                        }
                    }

                    event.jda.addEventListener(configListener)
                    Response.Volatile(message)
                }
            }
            "reload" -> {
                ConfigDirector.reloadServerConfig(event.guild)
                Response.Volatile("Reloaded config!")
            }
            else -> Response.Volatile("I'm not sure what you want to do with your config")
        }
    }

    private fun toMicro(guild: Guild): MicroConfig.Builder {
        val microConfig = MicroConfig.Builder()
        val config = guild.config

        // Wiki
        microConfig.startSection()
        config.wiki.apply {
            microConfig.push(minimumQuality)
            sources.forEach { microConfig.push(it) }
        }

        // Selectable roles
        microConfig.startSection()
        config.selectableRoles.apply {
            microConfig.push(limit)
            roles.forEach { microConfig.push(it) }
        }

        // Quickview
        microConfig.startSection()
        config.quickview.apply {
            microConfig.push(furaffinityEnabled, furaffinityThumbnails, picartoEnabled)
        }

        // Auditing
        microConfig.startSection()
        config.auditing.apply {
            microConfig.push(joins, leaves, purge, kicks, bans, names)
            microConfig.push(channel)
        }

        // Starboard
        microConfig.startSection()
        config.starboard.apply {
            microConfig.push(enabled, allowSelfStarring)
            microConfig.push(threshold)
            microConfig.push(emoji)
            microConfig.push(channel)
        }

        // Server info
        microConfig.push(guild.idLong)
        microConfig.push(guild.name)

        // Channels
        microConfig.startSection()
        guild.textChannels.forEach {
            microConfig.push(it.name)
            microConfig.push(it.idLong)
        }

        // Roles
        microConfig.startSection()
        guild.roles.forEach {
            microConfig.push(it.name)
            // microConfig.push(it.position)
            // microConfig.push(event.guild.selfMember.canInteract(it))
            microConfig.push(it.idLong)
        }

        return microConfig
    }

    // TODO: Either get rid of magic numbers or take mercy and put MicroConfig to rest
    private fun fromMicro(microConfig: MicroConfig.Reader): ServerConfig {
        val quickviewBooleans = microConfig.pullBooleans(2, 0, 3)
        val auditingBooleans = microConfig.pullBooleans(3, 0, 6)
        val starboardBooleans = microConfig.pullBooleans(4, 0, 2)

        return ServerConfig(
            WikiConfig(
                microConfig.pullStringList(0, 1),
                microConfig.pullInt(0, 0) ?: 50
            ),
            SelectableRolesConfig(
                microConfig.pullLongList(1, 1),
                microConfig.pullInt(1, 0) ?: 1
            ),
            QuickviewConfig(quickviewBooleans[0], quickviewBooleans[1], quickviewBooleans[2]),
            AuditingConfig(
                auditingBooleans[0], auditingBooleans[1], auditingBooleans[3],
                auditingBooleans[4], auditingBooleans[5], auditingBooleans[6]
            ),
            StarboardConfig(
                starboardBooleans[0], microConfig.pullLong(4, 1),
                microConfig.pullString(4, 2) ?: "star",
                microConfig.pullInt(4, 3) ?: 1,
                starboardBooleans[1]
            )
        )
    }
}

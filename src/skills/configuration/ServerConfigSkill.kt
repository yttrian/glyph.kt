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

import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import kotlinx.coroutines.sync.Mutex
import me.ianmooreis.glyph.ai.AIResponse
import me.ianmooreis.glyph.directors.config.ConfigDirector
import me.ianmooreis.glyph.directors.skills.Skill
import me.ianmooreis.glyph.extensions.config
import me.ianmooreis.glyph.messaging.Response
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

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
    private val client = HttpClient {
        install(JsonFeature)
    }

    override suspend fun onTrigger(event: MessageReceivedEvent, ai: AIResponse): Response {
        val action = ai.result.getStringParameter("action")
        val config = event.guild.config
        val guild = event.guild

        return when (action) {
            "get" -> {
                val json = config.toJSON(guild)

                val microConfig = MicroConfig.Builder()
                val wait = Mutex(true)

                // Server info
                microConfig.push(guild.idLong)
                microConfig.push(guild.name)

                // Channels
                microConfig.startSection()
                guild.textChannels.forEach {
                    microConfig.push(it.name)
                    microConfig.push(it.idLong)
                }

                // Emojis
                microConfig.startSection()
                guild.retrieveEmotes().queue {
                    it.forEach { emote ->
                        microConfig.push(emote.name)
                        // microConfig.push(emote.idLong)
                    }
                    wait.unlock()
                }
                wait.lock()

                // Roles
                microConfig.startSection()
                guild.roles.forEach {
                    microConfig.push(it.name)
                    // microConfig.push(it.position)
                    // microConfig.push(event.guild.selfMember.canInteract(it))
                    microConfig.push(it.idLong)
                }

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

                microConfig.push(null)

                val msgPack = microConfig.build()

                println(msgPack)

                if (msgPack.length > Message.MAX_CONTENT_LENGTH) {
                    Response.Volatile("Whoops, I'm stupid!")
                } else {
                    Response.Volatile(msgPack)
                }

                // event.message.reply(json)
                // Myjson.postJSON(json, 2000) { key ->
                //     if (key !== null) {
                //         event.message.reply(
                //             EmbedBuilder()
                //                 .setTitle("Glyph Config Editor")
                //                 .setDescription(
                //                     "Click the link below to edit your config. " +
                //                             "When done on the editor, click Save and then Copy Key. " +
                //                             "Come back here and tell Glyph to \"load config <key>\" with the key " +
                //                             "you were given.\n" +
                //                             "[Edit Config](https://gl.yttr.org/config#$key)"
                //                 )
                //                 .build()
                //         )
                //     } else {
                //         event.message.reply("Unable to upload config for editing, try again later.")
                //     }
                // }
            }
            "set" -> {
                val key = ai.result.getStringParameter("key")

                // if (key !== null) {
                //     Myjson.getJSON(key, 2000) { result ->
                //         when (result) {
                //             is Result.Success -> {
                //                 val newConfig = config.fromJSON(result.get())
                //                 ConfigDirector.setServerConfig(event.guild, newConfig)
                //
                //                 event.message.reply("Successfully updated server config!")
                //             }
                //             is Result.Failure -> {
                //                 event.message.reply("Could not retrieve config or it was malformed!")
                //             }
                //         }
                //     }
                // }
                Response.None
            }
            "reload" -> {
                ConfigDirector.reloadServerConfig(event.guild)
                Response.Volatile("Reloaded config!")
            }
            else -> Response.Volatile("I'm not sure what you want to do with your config")
        }
    }
}

/*
 * Guild.kt
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

package me.ianmooreis.glyph.bot.extensions

import me.ianmooreis.glyph.bot.database.config.ConfigDirector
import me.ianmooreis.glyph.bot.database.config.server.ServerConfig
import me.ianmooreis.glyph.bot.directors.messaging.SimpleDescriptionBuilder
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import org.ocpsoft.prettytime.PrettyTime
import java.awt.Color
import java.time.Instant

/**
 * The configuration of a guild (either custom or default if no custom one found)
 */
val Guild.config: ServerConfig
    get(): ServerConfig = ConfigDirector.getServerConfig(this)

/**
 * Delete a guild's configuration from the database
 */
suspend fun Guild.deleteConfig() {
    ConfigDirector.deleteServerConfig(this)
}

/**
 * Attempt to find a user in a guild by their effective name, username, nickname, and/or id
 *
 * @param search the value to use to try and find a user
 *
 * @return a user or null if not found
 */
fun Guild.findUser(search: String): User? {
    return this.getMembersByEffectiveName(search, true).firstOrNull()?.user
        ?: this.getMembersByName(search, true).firstOrNull()?.user
        ?: this.getMembersByNickname(search, true).firstOrNull()?.user ?: try {
            this.jda.getUserById(search)
        } catch (e: NumberFormatException) {
            null
        }
}

/**
 * Get an informational embed about a server
 *
 * @param title  the title of the embed
 * @param footer any footer text to include in the embed
 * @param color  the color of the embed
 * @param showExactCreationDate whether or not to show the exact timestamp for the server creation time
 *
 * @return an embed with the requested server info
 */
fun Guild.getInfoEmbed(
    title: String?,
    footer: String?,
    color: Color?,
    showExactCreationDate: Boolean = false
): MessageEmbed {
    val createdAgo = PrettyTime().format(this.timeCreated.toDate())
    val overviewDescription = SimpleDescriptionBuilder()
        .addField("Name", this.name)
        .addField("ID", this.id)
        .addField("Region", this.regionRaw)
        .addField("Created", "$createdAgo ${if (showExactCreationDate) "(${this.timeCreated})" else ""}")
        .addField("Owner", this.owner?.asMention ?: "?")
        .build()
    val membersDescription = SimpleDescriptionBuilder()
        .addField("Humans", this.members.count { !it.user.isBot })
        .addField("Bots", this.members.count { it.user.isBot })
        .addField("Online", this.members.count { it.onlineStatus == OnlineStatus.ONLINE })
        .addField("Total", this.members.count())
        .build()
    val channelsDescription = SimpleDescriptionBuilder()
        .addField("Text", this.textChannels.count())
        .addField("Voice", this.voiceChannels.count())
        .addField("Categories", this.categories.count())
        .build()
    val rolesDescription = SimpleDescriptionBuilder()
        .addField("Total", this.roles.count())
        .addField("List", this.roles.joinToString { it.asMention })
        .build()
    return EmbedBuilder().setTitle(title)
        .addField("Overview", overviewDescription, false)
        .addField("Members", membersDescription, true)
        .addField("Channels", channelsDescription, true)
        .addField("Roles", rolesDescription, true)
        .setThumbnail(this.iconUrl)
        .setFooter(footer, null)
        .setColor(color)
        .setTimestamp(Instant.now())
        .build()
}

/*
 * User.kt
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

package org.yttr.glyph.bot.extensions

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import org.ocpsoft.prettytime.PrettyTime
import org.yttr.glyph.bot.directors.messaging.SimpleDescriptionBuilder
import java.awt.Color
import java.time.Instant

/**
 * Get an informational embed about a user
 *
 * @param title  the title of the embed
 * @param footer any footer text to include in the embed
 * @param color  the color of the embed
 * @param showExactCreationDate whether or not to show the exact timestamp for the user's creation time
 * @param mutualGuilds whether or not to show how many guilds are shared with the user
 *
 * @return an embed with the requested user info
 */
fun User.getInfoEmbed(
    title: String?,
    footer: String?,
    color: Color?,
    showExactCreationDate: Boolean = false,
    mutualGuilds: Boolean = false
): MessageEmbed {
    val botTag = if (this.isBot) "(bot)" else ""
    val createdAgo = PrettyTime().format(this.timeCreated.toDate())
    val descriptionBuilder: SimpleDescriptionBuilder = SimpleDescriptionBuilder()
        .addField("User", "${this.asPlainMention} $botTag")
        .addField("ID", this.id)
        .addField("Mention", this.asMention)
        .addField("Created", "$createdAgo ${if (showExactCreationDate) "(${this.timeCreated})" else ""}")
    if (mutualGuilds) {
        descriptionBuilder.addField("Server", "${this.mutualGuilds.size} mutual")
    }
    return EmbedBuilder().setTitle(title)
        .setDescription(descriptionBuilder.build())
        .setThumbnail(this.avatarUrl)
        .setFooter(footer, null)
        .setColor(color)
        .setTimestamp(Instant.now())
        .build()
}

/**
 * Gets a string of the username with the discriminator
 */
val User.asPlainMention: String
    get() = "${this.name}#${this.discriminator}"

/**
 * Gets a string of the username with the discriminator
 */
val Member.asPlainMention: String
    get() = this.user.asPlainMention

/**
 * Reports if a user if the creator
 */
val User.isCreator: Boolean
    get() = this.idLong == System.getenv("CREATOR_ID").toLong()

/**
 * Send a user a PM before an action where they might not be seen again (kick/ban)
 */
fun User.sendDeathPM(message: String, die: () -> Unit) {
    if (!this.isBot) {
        this.openPrivateChannel().queue { pm ->
            pm.sendMessage(message).queue({
                pm.close().queue {
                    die()
                }
            }, { die() })
        }
    } else {
        die()
    }
}

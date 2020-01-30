/*
 * Auditing.kt
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

package me.ianmooreis.glyph.extensions

import me.ianmooreis.glyph.directors.WebhookDirector
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.SelfUser
import java.awt.Color
import java.time.Instant

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
        val channel = this.getTextChannelById(channelID)  // channel must belong to server
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

/**
 * Sends an embed to the global log webhook
 *
 * @param title the title of the embed
 * @param description the body of the embed
 * @param color the color of the embed
 */
fun SelfUser.log(title: String, description: String, color: Color? = null) {
    WebhookDirector.send(
        this, System.getenv("LOGGING_WEBHOOK"),
        EmbedBuilder()
            .setTitle(title)
            .setDescription(description)
            .setFooter("Logging", null)
            .setColor(color)
            .setTimestamp(Instant.now())
            .build()
    )
}

/**
 * Sends an embed to the global log webhook
 *
 * @param embed the embed to send
 */
fun SelfUser.log(embed: MessageEmbed) {
    WebhookDirector.send(this, System.getenv("LOGGING_WEBHOOK"), embed)
}
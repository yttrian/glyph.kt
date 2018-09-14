/*
 * Channel.kt
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

package me.ianmooreis.glyph.orchestrators.messaging.quickview.picarto

import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.MessageEmbed
import java.awt.Color
import java.net.URL
import java.time.Instant

/**
 * A Picarto channel
 */
class Channel(
    /**
     * The channel name
     */
    val name: String, private val avatar: URL,
    private val viewers: Int, private val followers: Int, private val category: String, private val title: String,
    private val online: Boolean, private val adult: Boolean,
    private val tags: List<String>) {

    /**
     * Creates an embed with the channel's info
     */
    fun getEmbed(): MessageEmbed {
        val url = "https://picarto.tv/$name"
        return EmbedBuilder()
            .setTitle(this.title, url)
            .setAuthor(this.name, url)
            .setDescription(
                "**Status** ${if (online) "Online" else "Offline"}\n" +
                    "**Category** $category (${if (adult) "NSFW" else "SFW"})\n" +
                    "**Viewers** $viewers | **Followers** $followers")
            .addField("Tags", tags.joinToString(), false)
            .setThumbnail(avatar.toString())
            .setColor(if (online) Color.GREEN else Color.RED)
            .setFooter("picarto", null)
            .setTimestamp(Instant.now())
            .build()
    }
}
/*
 * Submission.kt
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

package me.ianmooreis.glyph.directors.messaging.quickview.furaffinity

import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.MessageEmbed
import java.net.URL
import java.util.Date

/**
 * A FurAffinity submission
 */
class Submission(private val title: String, private val name: String, private val profile: URL,
    /**
     * The direct link to the submission
     */
    val link: URL,
    private val posted_at: Date, private val download: URL, private val full: URL,
    private val category: String, private val theme: String, private val species: String?, private val gender: String?,
    private val favorites: Int, private val comments: Int, private val views: Int, private val resolution: String?,
    /**
     * The submission rating (maturity level) of the submission
     */
    val rating: SubmissionRating,
    private val keywords: List<String>) {

    /**
     * Creates an embed with the submission's info and a thumbnail if desired
     */
    fun getEmbed(thumbnail: Boolean): MessageEmbed {
        val linkedKeywords = keywords.joinToString { "[$it](https://www.furaffinity.net/search/@keywords%20$it)" }
        val fancyKeywords = if (linkedKeywords.length < 1024) linkedKeywords else keywords.joinToString()
        val fileType = download.toString().substringAfterLast(".")
        return EmbedBuilder()
            .setTitle(title, link.toString())
            .setThumbnail(if (thumbnail) full.toString() else null)
            .setDescription(
                "**Category** $category > $theme (${rating.name})\n" +
                    (if (species != null) "**Species** $species\n" else "") +
                    (if (gender != null) "**Gender** $gender\n" else "") +
                    "**Favorites** $favorites | **Comments** $comments | **Views** $views" +
                    if ((thumbnail && rating.nsfw) || !rating.nsfw) "\n**Download** [${resolution
                        ?: fileType}]($download)" else "")
            .addField("Keywords", fancyKeywords, false)
            .setFooter("FurAffinity", null)
            .setColor(rating.color)
            .setAuthor(name, profile.toString())
            .setTimestamp(posted_at.toInstant())
            .build()
    }
}
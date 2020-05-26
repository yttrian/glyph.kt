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

package me.ianmooreis.glyph.messaging.quickview.furaffinity

import me.ianmooreis.glyph.directors.messaging.SimpleDescriptionBuilder
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import java.util.Date

/**
 * A FurAffinity submission
 */
data class Submission(
    /**
     * Title of the submission
     */
    val title: String,
    /**
     * Name of the poster
     */
    val name: String,
    /**
     * URL of the poster's profile
     */
    val profile: String,
    /**
     * URL of the poster's avatar
     */
    val avatar: String,
    /**
     * Direct link to the submission
     */
    val link: String,
    /**
     * Date the submission was posted
     */
    val posted_at: Date,
    /**
     * Download URL of the content
     */
    val download: String,
    /**
     * URL of the full resolution content
     */
    val full: String,
    /**
     * Category the submission is placed under
     */
    val category: String,
    /**
     * Theme of the submission
     */
    val theme: String,
    /**
     * Species specified in the submission
     */
    val species: String?,
    /**
     * Gender specified in the submission
     */
    val gender: String?,
    /**
     * Number of favorites the submission has received
     */
    val favorites: Int,
    /**
     * Number of comments the submission has received
     */
    val comments: Int,
    /**
     * Number of views the submission has received
     */
    val views: Int,
    /**
     * Resolution of the submission content is an image
     */
    val resolution: String?,
    /**
     * Submission rating (maturity level) of the submission
     */
    val rating: SubmissionRating,
    /**
     * Keywords assigned to the submission
     */
    val keywords: List<String>
) {

    /**
     * Creates an embed with the submission's info and a thumbnail if desired
     */
    fun getEmbed(thumbnail: Boolean): MessageEmbed {
        val linkedKeywords = keywords.joinToString { "[$it](https://www.furaffinity.net/search/@keywords%20$it)" }
        val fancyKeywords = if (linkedKeywords.length < 1024) linkedKeywords else keywords.joinToString()
        val fileType = download.substringAfterLast(".")
        val description = SimpleDescriptionBuilder()

        // Add the different fields to the quickview embed description
        description.addField("Category", "$category - $theme (${rating.name})")
        if (species != null) {
            description.addField("Species", species)
        }
        if (gender != null) {
            description.addField("Gender", gender)
        }
        description.addField(null, "**Favorites** $favorites | **Comments** $comments | **Views** $views")
        if ((thumbnail && rating.nsfw) || !rating.nsfw) {
            description.addField("Download", "[${resolution ?: fileType}]($download)")
        }

        return EmbedBuilder()
            .setTitle(title, link)
            .setThumbnail(if (thumbnail) full else null)
            .setDescription(description.build())
            .addField("Keywords", fancyKeywords, false)
            .setFooter("FurAffinity")
            .setColor(rating.color)
            .setAuthor(name, profile, avatar)
            .setTimestamp(posted_at.toInstant())
            .build()
    }
}
/*
 * FurAffinity.kt
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

import com.google.common.math.IntMath
import io.ktor.client.request.get
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.mapNotNull
import me.ianmooreis.glyph.directors.config.server.QuickviewConfig
import me.ianmooreis.glyph.extensions.config
import me.ianmooreis.glyph.extensions.contentClean
import me.ianmooreis.glyph.messaging.quickview.QuickviewGenerator
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.math.RoundingMode

/**
 * Handles the creation of QuickViews for furaffinity.net links
 */
class FurAffinityGenerator : QuickviewGenerator() {
    companion object {
        private const val API_HOST: String = "https://faexport.spangle.org.uk"
        private const val GALLERY_LISTING_SIZE: Int = 72

        /**
         * Represents a user page in the API
         */
        data class UserPage(
            /**
             * Total number of submissions the user has
             */
            val submissions: Int
        )

        /**
         * Represents a submission excerpt from the submission listing endpoint of the API
         */
        data class SubmissionExcerpt(
            /**
             * Submission id
             */
            val id: Int,
            /**
             * Submission thumbnail URL
             */
            val thumbnail: String
        )
    }

    private val submissionUrlRegex =
        Regex("(furaffinity.net/view/(\\d{8}))|(d.facdn.net/art/(\\w*)/(\\d{10}))", RegexOption.IGNORE_CASE)

    override suspend fun generate(event: MessageReceivedEvent, config: QuickviewConfig): Flow<MessageEmbed> {
        val content = event.message.contentClean

        val submissionIds = findIds(content)

        return submissionIds.mapNotNull {
            getSubmission(it)?.run {
                // allow only SFW thumbnails in DMs, and all in enabled servers but only show NSFW in NSFW channels
                val allowThumbnail = (!event.isFromGuild && !rating.nsfw) ||
                    (event.guild.config.quickview.furaffinityThumbnails && (event.textChannel.isNSFW || !rating.nsfw))

                getEmbed(allowThumbnail)
            }
        }
    }

    /**
     * Attempts to find ids associated with FurAffinity submissions, if there are any
     */
    fun findIds(content: String): Flow<Int> {
        return submissionUrlRegex.findAll(content).distinct().asFlow().mapNotNull {
            val submissionId = it.groups[2]?.value?.toInt()
            val cdnId = it.groups[5]?.value?.toInt()
            val username = it.groups[4]?.value

            when {
                submissionId != null -> submissionId
                cdnId != null && username != null -> findSubmissionId(cdnId, username)
                else -> null
            }
        }
    }

    /**
     * Try to find a submission using its CDN ID by searching the poster's gallery for it
     */
    suspend fun findSubmissionId(cdnId: Int, user: String): Int? {
        val cdnIdString = cdnId.toString()

        val submissionCount = client.get<UserPage>("$API_HOST/user/$user.json").submissions
        val maxPages = IntMath.divide(submissionCount, GALLERY_LISTING_SIZE, RoundingMode.CEILING)

        for (page in 1..maxPages) {
            val listing = client.get<List<SubmissionExcerpt>>("$API_HOST/user/$user/gallery.json?full=1&page=1")
            listing.find { it.thumbnail.contains(cdnIdString) }?.let {
                return it.id
            }
        }

        return null
    }

    /**
     * Create a submission object given its ID
     */
    suspend fun getSubmission(id: Int): Submission? = client.get("$API_HOST/submission/$id.json")
}
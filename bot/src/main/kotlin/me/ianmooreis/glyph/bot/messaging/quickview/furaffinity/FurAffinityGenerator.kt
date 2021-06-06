/*
 * FurAffinityGenerator.kt
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

package me.ianmooreis.glyph.bot.messaging.quickview.furaffinity

import com.google.common.math.IntMath
import io.ktor.client.features.ClientRequestException
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.takeFrom
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.Serializable
import me.ianmooreis.glyph.bot.messaging.quickview.QuickviewGenerator
import me.ianmooreis.glyph.shared.config.server.QuickviewConfig
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.math.RoundingMode

/**
 * Handles the creation of QuickViews for furaffinity.net links
 */
class FurAffinityGenerator : QuickviewGenerator() {
    companion object {
        private const val API_BASE: String = "https://faexport.spangle.org.uk"
        private const val GALLERY_LISTING_SIZE: Int = 72

        /**
         * Represents a user page in the API
         */
        @Serializable
        data class UserPage(
            /**
             * Total number of submissions the user has
             */
            val submissions: Int
        )

        /**
         * Represents a submission excerpt from the submission listing endpoint of the API
         */
        @Serializable
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

    override suspend fun generate(event: MessageReceivedEvent, config: QuickviewConfig): Flow<MessageEmbed> =
        if (config.furaffinityEnabled) findIds(event.message.contentRaw).mapNotNull {
            getSubmission(it)?.run {
                // allow NSFW quickviews only in NSFW channels, never SFW channels or DMs
                val nsfwAllowed = event.isFromGuild && event.textChannel.isNSFW
                // allow thumbnails in DMs and in enabled servers
                val thumbnailAllowed = !event.isFromGuild || config.furaffinityThumbnails

                getEmbed(nsfwAllowed, thumbnailAllowed)
            }
        } else emptyFlow()

    /**
     * Attempts to find ids associated with FurAffinity submissions, if there are any
     */
    fun findIds(content: String): Flow<Int> =
        submissionUrlRegex.findAll(content).distinct().asFlow().mapNotNull {
            val submissionId = it.groups[2]?.value?.toInt()
            val cdnId = it.groups[5]?.value?.toInt()
            val username = it.groups[4]?.value

            when {
                submissionId != null -> submissionId
                cdnId != null && username != null -> findSubmissionId(cdnId, username)
                else -> null
            }
        }

    /**
     * Try to find a submission using its CDN ID by searching the poster's gallery for it
     */
    suspend fun findSubmissionId(cdnId: Int, user: String): Int? {
        val cdnIdString = cdnId.toString()

        val submissionCount = client.get<UserPage> {
            url.takeFrom(API_BASE).path("user", "$user.json")
        }.submissions
        val maxPages = IntMath.divide(submissionCount, GALLERY_LISTING_SIZE, RoundingMode.CEILING)

        for (page in 1..maxPages) {
            val listing = client.get<List<SubmissionExcerpt>> {
                url.takeFrom(API_BASE).path("user", user, "gallery.json")
                parameter("full", "1")
                parameter("page", page)
            }

            listing.find { it.thumbnail.contains(cdnIdString) }?.let {
                return it.id
            }
        }

        return null
    }

    /**
     * Create a submission object given its ID
     */
    suspend fun getSubmission(id: Int): Submission? = try {
        client.get {
            url.takeFrom(API_BASE).path("submission", "$id.json")
        }
    } catch (e: ClientRequestException) {
        null
    }
}

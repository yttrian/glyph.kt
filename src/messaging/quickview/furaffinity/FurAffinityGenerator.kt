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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
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
    }

    private val standardUrlFormat =
        Regex("((http[s]?)://)?(www.)?(furaffinity.net)/(\\w*)/(\\d{8})/?", RegexOption.IGNORE_CASE)
    private val cdnUrlFormat =
        Regex("(http[s]?):/{2}(d.facdn.net)/art/(.*)/(\\d{10})/.*(.png|.jp[e]?g)", RegexOption.IGNORE_CASE)

    @ExperimentalCoroutinesApi
    override suspend fun generate(event: MessageReceivedEvent, config: QuickviewConfig): Flow<MessageEmbed> {
        val content = event.message.contentClean

        val standardSubmissionIds: Flow<Int> =
            standardUrlFormat.findAll(content).asFlow().mapNotNull { it.groups[6]?.value?.toInt() }
        val cdnSubmissionIds: Flow<Int> = cdnUrlFormat.findAll(content).asFlow().mapNotNull {
            val cdnId = it.groups[4]?.value?.toInt()
            val username = it.groups[3]?.value

            if (cdnId != null && username != null) findSubmissionId(cdnId, username) else null
        }
        val submissionIds = merge(standardSubmissionIds, cdnSubmissionIds)

        return submissionIds.mapNotNull {
            getSubmission(it)?.run {
                // allow only SFW thumbnails in DMs, and all in enabled servers but only show NSFW in NSFW channels
                val allowThumbnail = (!event.isFromGuild && !rating.nsfw) ||
                    (event.guild.config.quickview.furaffinityThumbnails && (event.textChannel.isNSFW || !rating.nsfw))

                getEmbed(allowThumbnail)
            }
        }
    }

    private suspend fun findSubmissionId(cdnId: Int, user: String): Int? {
        val cdnIdString = cdnId.toString()

        data class UserPage(val submissions: Int)

        val submissionCount = client.get<UserPage>("$API_HOST/user/$user.json").submissions
        val maxPages = IntMath.divide(submissionCount, GALLERY_LISTING_SIZE, RoundingMode.CEILING)

        data class SubmissionExcerpt(val id: Int, val thumbnail: String)
        data class SubmissionListing(val submissions: List<SubmissionExcerpt>)

        for (page in 1..maxPages) {
            val listing = client.get<SubmissionListing>("$API_HOST/user/$user/gallery.json?full=1&page=1")
            listing.submissions.find { it.thumbnail.contains(cdnIdString) }?.let {
                return it.id
            }
        }

        return null
    }

    private suspend fun getSubmission(id: Int): Submission? = client.get("$API_HOST/submission/$id.json")
}
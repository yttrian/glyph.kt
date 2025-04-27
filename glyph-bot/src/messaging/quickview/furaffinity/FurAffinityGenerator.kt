package org.yttr.glyph.bot.messaging.quickview.furaffinity

import com.google.common.math.IntMath
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.path
import io.ktor.http.takeFrom
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.Serializable
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.yttr.glyph.bot.messaging.quickview.QuickviewGenerator
import org.yttr.glyph.shared.config.server.QuickviewConfig
import java.math.RoundingMode

/**
 * Handles the creation of QuickViews for furaffinity.net links
 */
object FurAffinityGenerator : QuickviewGenerator() {
    private const val API_BASE: String = "https://faexport.spangle.org.uk"
    private const val GALLERY_LISTING_SIZE: Int = 72

    override val urlRegex: Regex = Regex(
        "\\b(furaffinity.net/(?:full|view)/(\\d+))|(d\\.(?:facdn|furaffinity).net/art/([\\w-]+)/(\\d+))\\b",
        RegexOption.IGNORE_CASE
    )

    private const val SUBMISSION_URL_REGEX_SUBMISSION_ID_GROUP: Int = 2
    private const val SUBMISSION_URL_REGEX_CDN_ID_GROUP: Int = 5
    private const val SUBMISSION_URL_REGEX_USERNAME_GROUP: Int = 4

    private val escapedLinkRegex = Regex("<\\S+>|`.+`", RegexOption.DOT_MATCHES_ALL)

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

    override suspend fun generate(event: MessageReceivedEvent, config: QuickviewConfig): Flow<MessageEmbed> =
        if (config.furaffinityEnabled) findIds(event.message.contentRaw).mapNotNull {
            getSubmission(it)?.run {
                // allow NSFW quickviews only in NSFW channels, never SFW channels or DMs
                val nsfwAllowed = event.isFromGuild && event.textChannel.isNSFW
                // allow thumbnails unless disabled (default on in servers and DMs)
                val thumbnailAllowed = config.furaffinityThumbnails

                getEmbed(nsfwAllowed, thumbnailAllowed)
            }
        } else emptyFlow()

    private data class SubmissionUrlData(val submissionId: Int?, val cdnId: Int?, val username: String?)

    /**
     * Attempts to find ids associated with Fur Affinity submissions, if there are any
     */
    fun findIds(content: String): Flow<Int> =
        urlRegex.findAll(content.replace(escapedLinkRegex, "")).map {
            val submissionId = it.groups[SUBMISSION_URL_REGEX_SUBMISSION_ID_GROUP]?.value?.toInt()
            val cdnId = it.groups[SUBMISSION_URL_REGEX_CDN_ID_GROUP]?.value?.toInt()
            val username = it.groups[SUBMISSION_URL_REGEX_USERNAME_GROUP]?.value
            SubmissionUrlData(submissionId, cdnId, username)
        }.distinct().asFlow().mapNotNull {
            when {
                it.submissionId != null -> it.submissionId
                it.cdnId != null && it.username != null -> findSubmissionId(it.cdnId, it.username)
                else -> null
            }
        }

    /**
     * Try to find a submission using its CDN ID by searching the poster's gallery for it
     */
    suspend fun findSubmissionId(cdnId: Int, user: String): Int? {
        val cdnIdString = cdnId.toString()

        val submissionCount = client.get {
            url.takeFrom(API_BASE).path("user", "$user.json")
        }.body<UserPage>().submissions
        val maxPages = IntMath.divide(submissionCount, GALLERY_LISTING_SIZE, RoundingMode.CEILING)

        for (page in 1..maxPages) {
            val listing = client.get {
                url.takeFrom(API_BASE).path("user", user, "gallery.json")
                parameter("full", "1")
                parameter("page", page)
            }.body<List<SubmissionExcerpt>>()

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
        }.body()
    } catch (e: ClientRequestException) {
        log.debug("Error getting submission data", e)
        null
    }
}

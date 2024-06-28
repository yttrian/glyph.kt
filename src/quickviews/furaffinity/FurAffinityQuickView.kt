package org.yttr.glyph.quickviews.furaffinity

import dev.kord.core.entity.Message
import dev.kord.rest.builder.message.EmbedBuilder
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.path
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import org.yttr.glyph.quickviews.QuickView
import org.yttr.glyph.quickviews.isNsfwAllowed

/**
 * Handles the creation of QuickViews for furaffinity.net links
 */
data class FurAffinityQuickView(val url: SubmissionUrlData, val message: Message) : QuickView {
    data class SubmissionUrlData(val submissionId: Int?, val cdnId: Int?, val username: String?)

    override suspend fun build(): EmbedBuilder? = when {
        url.submissionId != null -> url.submissionId
        url.cdnId != null && url.username != null -> findSubmissionId(url.cdnId, url.username)
        else -> null
    }?.let { getSubmission(it) }?.getEmbed(
        // allow NSFW quickviews only in NSFW channels, never SFW channels or DMs
        nsfwAllowed = message.isNsfwAllowed(),
        // allow thumbnails unless disabled (default on in servers and DMs)
        thumbnailAllowed = false // TODO: Check config
    )

    /**
     * Try to find a submission using its CDN ID by searching the poster's gallery for it
     */
    private suspend fun findSubmissionId(cdnId: Int, user: String): Int? {
        val cdnIdString = cdnId.toString()

        val submissionCount = client.get {
            url.path("user/$user.json")
        }.body<UserPage>().submissions

        val maxPages = submissionCount / GALLERY_LISTING_SIZE // TODO: Round up

        for (page in 1..maxPages) {
            val listing = client.get {
                url.path("user/$user/gallery.json")
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
    private suspend fun getSubmission(id: Int): Submission? = try {
        client.get {
            url.path("submission/$id.json")
        }.body()
    } catch (e: ClientRequestException) {
        log.debug("Error getting submission data", e)
        null
    }

    companion object : QuickView.Scanner<FurAffinityQuickView> {
        private val log = LoggerFactory.getLogger(FurAffinityQuickView::class.java)

        private const val GALLERY_LISTING_SIZE: Int = 72
        private const val SUBMISSION_URL_REGEX_SUBMISSION_ID_GROUP: Int = 2
        private const val SUBMISSION_URL_REGEX_CDN_ID_GROUP: Int = 5
        private const val SUBMISSION_URL_REGEX_USERNAME_GROUP: Int = 4

        val client = HttpClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }

            defaultRequest {
                url("https://faexport.spangle.org.uk")
            }
        }

        private val urlRegex = Regex(
            pattern = "\\b(furaffinity.net/(?:full|view)/(\\d+))|(d\\.(?:facdn|furaffinity).net/art/([\\w-]+)/(\\d+))\\b",
            option = RegexOption.IGNORE_CASE
        )

        private val escapedLinkRegex = Regex("<\\S+>|`.+`", RegexOption.DOT_MATCHES_ALL)

        /**
         * Attempts to find ids associated with Fur Affinity submissions, if there are any
         */
        private fun findIds(content: String): List<SubmissionUrlData> =
            urlRegex.findAll(content.replace(escapedLinkRegex, "")).map {
                val submissionId = it.groups[SUBMISSION_URL_REGEX_SUBMISSION_ID_GROUP]?.value?.toInt()
                val cdnId = it.groups[SUBMISSION_URL_REGEX_CDN_ID_GROUP]?.value?.toInt()
                val username = it.groups[SUBMISSION_URL_REGEX_USERNAME_GROUP]?.value

                SubmissionUrlData(submissionId, cdnId, username)
            }.distinct().toList()

        override fun scan(message: Message): List<FurAffinityQuickView> =
            findIds(message.content).map {
                FurAffinityQuickView(it, message)
            }
    }
}

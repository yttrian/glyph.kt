package org.yttr.glyph.messaging.quickview.furaffinity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import org.yttr.glyph.directors.messaging.SimpleDescriptionBuilder
import java.time.Instant

/**
 * A Fur Affinity submission
 */
@Serializable
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
    @SerialName("posted_at")
    val postedAt: String,
    /**
     * Download URL of the content
     */
    val download: String?,
    /**
     * URL of the full resolution content
     */
    val full: String?,
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
    fun getEmbed(nsfwAllowed: Boolean, thumbnailAllowed: Boolean): MessageEmbed {
        val embed = EmbedBuilder()
            .setAuthor(name, profile, avatar)
            .setTitle(title, link)
            .setColor(rating.color)
            .setFooter("Fur Affinity")
            .setTimestamp(Instant.parse(postedAt))

        val description = SimpleDescriptionBuilder()

        // Add the different fields to the quickview embed description
        description.addField("Category", "$category - $theme (${rating.name})")
        species?.let { description.addField("Species", it) }
        gender?.let { description.addField("Gender", it) }
        description.addField(null, "**Favorites** $favorites | **Comments** $comments | **Views** $views")

        if (rating.nsfw && !nsfwAllowed) {
            val warningText = "Submissions with a rating of $rating cannot be previewed outside of a NSFW channel!"
            embed.addField("Warning", warningText, false)
        } else {
            if (thumbnailAllowed) {
                embed.setImage(full)
            }

            addDownloadDescription(description)

            // Try making all keywords linked, but if too long just make them truncated text
            val linkedKeywords = keywords.joinToString { "[$it](https://www.furaffinity.net/search/@keywords%20$it)" }
            val fancyKeywords = if (linkedKeywords.length < MessageEmbed.VALUE_MAX_LENGTH) {
                linkedKeywords
            } else {
                keywords.joinToString(limit = MessageEmbed.VALUE_MAX_LENGTH)
            }

            // Only show keywords if there are any
            if (fancyKeywords.isNotBlank()) {
                embed.addField("Keywords", fancyKeywords, false)
            }
        }

        return embed.setDescription(description.build()).build()
    }

    private fun addDownloadDescription(descriptionBuilder: SimpleDescriptionBuilder) {
        // If there is a download link, add it
        if (download != null) {
            val fileType = download.substringAfterLast(".")
            val validFileType = fileType.isNotBlank() && fileType.length <= MAX_FILE_EXTENSION_LENGTH
            // Try to use the resolution and file extension when possible
            val downloadText = when {
                resolution != null && validFileType -> "$resolution (.$fileType)"
                resolution != null -> resolution
                validFileType -> fileType
                else -> "Download"
            }
            descriptionBuilder.addField("Download", "[$downloadText]($download)")
        }
    }

    companion object {
        // Some files don't have a real extension, let's pretend proper files have at most 4 chars in their extension
        private const val MAX_FILE_EXTENSION_LENGTH = 4
    }
}

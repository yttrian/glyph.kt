package org.yttr.glyph.quickviews.furaffinity

import dev.kord.rest.builder.message.EmbedBuilder
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.yttr.glyph.SimpleDescriptionBuilder

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
    @SerialName("posted_at") val postedAt: String,

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
)

// Some files don't have a real extension, let's pretend proper files have at most 4 chars in their extension
private const val MAX_FILE_EXTENSION_LENGTH = 4

/**
 * Creates an embed with the submission's info and a thumbnail if desired
 */
fun Submission.getEmbed(nsfwAllowed: Boolean, thumbnailAllowed: Boolean): EmbedBuilder {
    val submission = this
    val restricted = rating.nsfw && !nsfwAllowed

    return EmbedBuilder().apply {
        author {
            name = submission.name
            url = submission.profile
            icon = submission.avatar
        }

        title = submission.title
        url = submission.link
        color = submission.rating.color
        footer {
            text = "Fur Affinity"
        }
//        timestamp = TODO

        description = SimpleDescriptionBuilder {
            addField("Category", "$category - $theme (${rating.name})")
            submission.species?.let { addField("Species", it) }
            submission.gender?.let { addField("Gender", it) }
            addField(null, "**Favorites** $favorites | **Comments** $comments | **Views** $views")

            // If there is a download link, add it
            if (!restricted && submission.download != null) {
                val fileType = submission.download.substringAfterLast(".")
                val validFileType = fileType.isNotBlank() && fileType.length <= MAX_FILE_EXTENSION_LENGTH

                // Try to use the resolution and file extension when possible
                val downloadText = when {
                    resolution != null && validFileType -> "$submission.resolution (.$fileType)"
                    resolution != null -> submission.resolution
                    validFileType -> fileType
                    else -> "Download"
                }

                addField("Download", "[$downloadText]($submission.download)")
            }
        }

        if (restricted) {
            field("Warning") {
                "Submissions with a rating of $rating cannot be previewed outside of a NSFW channel!"
            }
        } else {
            if (thumbnailAllowed) {
                image = submission.full
            }

            if (submission.keywords.isNotEmpty()) {
                field("Keywords") { submission.keywords.joinToString() }
            }
        }
    }
}

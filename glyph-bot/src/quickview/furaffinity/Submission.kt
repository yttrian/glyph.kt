package org.yttr.glyph.bot.quickview.furaffinity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import org.jsoup.Jsoup
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
     * URL of the full resolution content
     */
    val full: String?,
    /**
     * HTML descriptipn
     */
    val description: String,
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
     * Submission rating (maturity level) of the submission
     */
    val rating: SubmissionRating,
) {

    /**
     * Creates an embed with the submission's info and a thumbnail if desired
     */
    fun getEmbed(nsfwAllowed: Boolean): MessageEmbed {
        val embed = EmbedBuilder()
            .setAuthor(name, profile, avatar)
            .setTitle(title, link)
            .setColor(rating.color)
            .setFooter("Fur Affinity", "https://www.furaffinity.net/themes/beta/img/banners/fa_logo.png?v2")
            .setTimestamp(Instant.parse(postedAt))

        if (rating.nsfw && !nsfwAllowed) {
            embed.setDescription("Cannot preview $rating submissions in a non-NSFW channel")
        } else {
            embed.setImage(full)

            embed.setDescription(buildString {
                val soup = Jsoup.parse(description)

                // Remove the submission footer
                soup.select(".submission-footer").remove()

                // Replace :iconusername: with the username
                soup.select(".iconusername").forEach { iconUsername ->
                    iconUsername.text(iconUsername.select("img").attr("alt"))
                }

                val text = soup.text().trim()
                append(text.take(DESCRIPTION_MAX_LENGTH))

                if (text.length > DESCRIPTION_MAX_LENGTH) {
                    append("...")
                }
            })

            embed.addField("Views", views.toString(), true)
            embed.addField("Comments", comments.toString(), true)
            embed.addField("Favorites", favorites.toString(), true)
        }

        return embed.build()
    }

    companion object {
        private const val DESCRIPTION_MAX_LENGTH: Int = 512
    }
}

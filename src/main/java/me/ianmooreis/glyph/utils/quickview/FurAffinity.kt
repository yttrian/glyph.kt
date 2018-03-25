package me.ianmooreis.glyph.utils.quickview

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import me.ianmooreis.glyph.extensions.config
import me.ianmooreis.glyph.extensions.contentClean
import me.ianmooreis.glyph.extensions.reply
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import org.json.JSONArray
import org.slf4j.Logger
import org.slf4j.simple.SimpleLoggerFactory
import java.awt.Color
import java.net.URL
import java.util.*

class Submission(private val title: String, private val description: String, private val name: String, private val profile: URL, val link: URL,
                 val posted: String, private val posted_at: Date, private val download: URL, private val full: URL, private val thumbnail: URL,
                 private val category: String, private val theme: String, private val species: String?, private val gender: String?,
                 private val favorites: Int, private val comments: Int, private val views: Int, private val resolution: String?, val rating: SubmissionRating,
                 private val keywords: List<String>) {
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
                        if ((thumbnail && rating.nsfw) || !rating.nsfw) "\n**Download** [${resolution ?: fileType}]($download)" else "")
                .addField("Keywords", fancyKeywords, false)
                .setFooter("FurAffinity", null)
                .setColor(rating.color)
                .setAuthor(name, profile.toString())
                .setTimestamp(posted_at.toInstant())
                .build()
    }
}

enum class SubmissionRating(val color: Color, val nsfw: Boolean) {
    General(Color.GREEN, false),
    Mature(Color.BLUE, true),
    Adult(Color.RED, true)
}

object FurAffinity {
    private val log : Logger = SimpleLoggerFactory().getLogger(this.javaClass.simpleName)
    private val standardUrlFormat = Regex("((http[s]?)://)?(www.)?(furaffinity.net)/(\\w*)/(\\d{8})/?", RegexOption.IGNORE_CASE)
    private val cdnUrlFormat = Regex("(http[s]?):/{2}(d.facdn.net)/art/(.*)/(\\d{10})/.*(.png|.jp[e]?g)", RegexOption.IGNORE_CASE)

    fun makeQuickviews(event: MessageReceivedEvent) {
        standardUrlFormat.findAll(event.message.contentClean).map { it.groups[6]!!.value.toInt() }
                .plus(cdnUrlFormat.findAll(event.message.contentClean).mapNotNull { findSubmissionId(it.groups[4]!!.value.toInt(), it.groups[3]!!.value) })
                .map { getSubmission(it) }
                .forEach {
                    if (it != null) {
                        val allowThumbnail = if (!event.channelType.isGuild && !it.rating.nsfw) true else
                            event.guild.config.quickview.furaffinityThumbnails && ((event.textChannel.isNSFW && it.rating.nsfw) || !it.rating.nsfw)
                        event.message.reply(it.getEmbed(allowThumbnail))
                        log.info("Created FurAffinity QuickView in ${event.guild} for submission ${it.link}")
                    }
                }
    }

    private fun findSubmissionId(cdnId: Int, user: String, maxPages: Int = 1): Int? {
        for (page in 1..maxPages) {
            val (_, _, result) = "https://faexport.boothale.net/user/$user/gallery.json?full=1&page=1".httpGet().responseString()
            val submissions = JSONArray(result.get())
            if (result is Result.Success) {
                for (i in 0.until(submissions.length() - 1)) {
                    val submission = submissions.getJSONObject(i)
                    if (submission.getString("thumbnail").contains(cdnId.toString())) {
                        return submission.getInt("id")
                    }
                }
            }
        }
        log.error("Failed to find FurAffinity image source with CDN ID $cdnId by $user in $maxPages page!")
        return null
    }

    private fun getSubmission(id: Int): Submission? { //TODO: Figure out how not to do it blocking, because async had errors
        val (_, _, result) = "https://faexport.boothale.net/submission/$id.json".httpGet().responseString()
        return when (result) {
            is Result.Success -> {
                Gson().fromJson(result.get(), Submission::class.java)
            }
            is Result.Failure -> {
                this.log.warn("Failed to get submission $id from FAExport!")
                return null
            }
        }
    }
}
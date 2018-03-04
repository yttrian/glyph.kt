package me.ianmooreis.glyph.utils.quickview

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import me.ianmooreis.glyph.extensions.contentClean
import me.ianmooreis.glyph.extensions.reply
import me.ianmooreis.glyph.orchestrators.config
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import org.slf4j.Logger
import org.slf4j.simple.SimpleLoggerFactory
import java.awt.Color
import java.net.URL
import java.util.*

class Submission(private val title: String, private val description: String, private val name: String, private val profile: URL, private val link: URL,
                 val posted: String, private val posted_at: Date, private val download: URL, private val full: URL, private val thumbnail: URL,
                 private val category: String, private val theme: String, private val species: String, private val gender: String,
                 private val favorites: Int, private val comments: Int, private val views: Int, private val resolution: String, val rating: SubmissionRating,
                 private val keywords: List<String>) {
    fun getEmbed(thumbnail: Boolean): MessageEmbed {
        val linkedKeywords = keywords.joinToString { "[$it](https://www.furaffinity.net/search/@keywords%20$it)" }
        val fancyKeywords = if (linkedKeywords.length < 1024) linkedKeywords else keywords.joinToString()
        return EmbedBuilder()
                .setTitle(title, link.toString())
                .setThumbnail(if (thumbnail) full.toString() else null)
                .appendDescription(
                        "**Category** $category > $theme\n" +
                        "**Species** $species\n" +
                        "**Gender** $gender\n" +
                        "**Favorites** $favorites | **Comments** $comments | **Views** $views\n")
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

    fun makeQuickviews(event: MessageReceivedEvent) {
        val urlFormat = Regex("((http[s]?)://)?(www.)?(furaffinity.net)/(\\w*)/(\\d{8})/?", RegexOption.IGNORE_CASE)
        urlFormat.findAll(event.message.contentClean)
                .map { getSubmission(it.groups[6]!!.value.toInt()) }
                .forEach {
                    if (it != null) {
                        val allowThumbnail = (event.guild.config.quickview.furaffinityThumbnails && ((event.textChannel.isNSFW && it.rating.nsfw) || !it.rating.nsfw))
                        event.message.reply(it.getEmbed(allowThumbnail))
                    }
                }
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
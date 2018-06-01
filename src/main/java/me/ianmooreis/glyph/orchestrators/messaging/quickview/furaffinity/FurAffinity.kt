package me.ianmooreis.glyph.orchestrators.messaging.quickview.furaffinity

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import me.ianmooreis.glyph.extensions.config
import me.ianmooreis.glyph.extensions.contentClean
import me.ianmooreis.glyph.extensions.reply
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import org.json.JSONArray
import org.slf4j.Logger
import org.slf4j.simple.SimpleLoggerFactory

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
                log.warn("Failed to get submission $id from FAExport!")
                return null
            }
        }
    }
}
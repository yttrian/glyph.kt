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

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import me.ianmooreis.glyph.directors.config.server.QuickviewConfig
import me.ianmooreis.glyph.extensions.config
import me.ianmooreis.glyph.extensions.contentClean
import me.ianmooreis.glyph.extensions.reply
import me.ianmooreis.glyph.messaging.quickview.QuickviewGenerator
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.json.JSONArray
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Handles the creation of QuickViews for furaffinity.net links
 */
class FurAffinityGenerator : QuickviewGenerator() {
    companion object {
        private const val API_HOST: String = "https://faexport.spangle.org.uk"
    }

    private val log: Logger = LoggerFactory.getLogger(this.javaClass.simpleName)
    private val standardUrlFormat =
        Regex("((http[s]?)://)?(www.)?(furaffinity.net)/(\\w*)/(\\d{8})/?", RegexOption.IGNORE_CASE)
    private val cdnUrlFormat =
        Regex("(http[s]?):/{2}(d.facdn.net)/art/(.*)/(\\d{10})/.*(.png|.jp[e]?g)", RegexOption.IGNORE_CASE)

    /**
     * Makes any QuickViews for links found in a message
     *
     * @param event the message event
     */
    fun makeQuickviews(event: MessageReceivedEvent) {
        standardUrlFormat.findAll(event.message.contentClean).map { it.groups[6]!!.value.toInt() }
            .plus(cdnUrlFormat.findAll(event.message.contentClean).mapNotNull {
                findSubmissionId(
                    it.groups[4]!!.value.toInt(),
                    it.groups[3]!!.value
                )
            })
            .map { getSubmission(it) }
            .forEach {
                if (it != null) {
                    val allowThumbnail = if (!event.channelType.isGuild && !it.rating.nsfw) true else
                        event.guild.config.quickview.furaffinityThumbnails && ((event.textChannel.isNSFW && it.rating.nsfw) || !it.rating.nsfw)
                    event.message.reply(it.getEmbed(allowThumbnail))
                    log.info("Created FurAffinity QuickView for submission ${it.link}")
                }
            }
    }

    override suspend fun generate(event: MessageReceivedEvent, config: QuickviewConfig): List<MessageEmbed> {
        val content = event.message.contentClean
        TODO("Not yet implemented")
    }

    private fun findSubmissionId(cdnId: Int, user: String, maxPages: Int = 1): Int? {
        for (page in 1..maxPages) {
            val (_, _, result) = "$API_HOST/user/$user/gallery.json?full=1&page=1".httpGet().responseString()
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
        val (_, _, result) = "$API_HOST/submission/$id.json".httpGet().responseString()
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
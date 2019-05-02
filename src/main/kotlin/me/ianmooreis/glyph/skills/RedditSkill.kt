/*
 * RedditSkill.kt
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

package me.ianmooreis.glyph.skills

import ai.api.model.AIResponse
import com.squareup.moshi.JsonDataException
import me.ianmooreis.glyph.Glyph
import me.ianmooreis.glyph.directors.messaging.CustomEmote
import me.ianmooreis.glyph.directors.skills.Skill
import me.ianmooreis.glyph.extensions.reply
import net.dean.jraw.ApiException
import net.dean.jraw.RedditClient
import net.dean.jraw.http.NetworkException
import net.dean.jraw.http.NoopHttpLogger
import net.dean.jraw.http.OkHttpNetworkAdapter
import net.dean.jraw.http.UserAgent
import net.dean.jraw.models.Submission
import net.dean.jraw.models.SubredditSort
import net.dean.jraw.oauth.Credentials
import net.dean.jraw.oauth.OAuthHelper
import net.dean.jraw.references.SubredditReference
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.jodah.expiringmap.ExpiringMap
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * A skill that attempts to show users an image from a subreddit
 */
object RedditSkill : Skill("skill.reddit") {
    private val client: RedditClient = OAuthHelper.automatic(
        OkHttpNetworkAdapter(UserAgent("discord", this.javaClass.simpleName, Glyph.version, "IanM_56")),
        Credentials.userless(
            System.getenv("REDDIT_CLIENT_ID"),
            System.getenv("REDDIT_CLIENT_SECRET"),
            UUID.randomUUID()
        )
    )
    private val imageCache: MutableMap<String, Queue<Submission>> =
        ExpiringMap.builder().maxSize(10).expiration(30, TimeUnit.MINUTES).build()

    init {
        this.client.logger = NoopHttpLogger()
    }

    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        // Send typing since this can take some time and we want to indicate we are paying attention
        event.channel.sendTyping().queue()
        // Try to get the multireddit name
        val multiredditName: String? = try {
            ai.result.getStringParameter("multireddit").replace("\\", "")
        } catch (e: IllegalStateException) {
            null
        }  // Sometimes it doesn't parse the string right
        if (multiredditName == null) {
            event.message.reply("${CustomEmote.XMARK} I did not understand what subreddit you were asking for!")
            return
        }
        // If we have a multireddit name, try getting the reference to it otherwise report the failure
        try {
            val subreddit: SubredditReference = client.subreddit(multiredditName)
            val submission: Submission? = getRandomImage(subreddit)
            // If we were actually able to grab an image, send it, if allowed
            if (submission != null) {
                val nsfwAllowed = if (event.channelType.isGuild) event.textChannel.isNSFW else false
                if ((submission.isNsfw && nsfwAllowed) || !submission.isNsfw) {
                    event.message.reply(
                        EmbedBuilder()
                            .setTitle(submission.title, "https://reddit.com${submission.permalink}")
                            .setImage(submission.url)
                            .setFooter("r/${submission.subreddit}", null)
                            .setTimestamp(Instant.now())
                            .build()
                    )
                } else {
                    event.message.reply("${CustomEmote.XMARK} I can only show NSFW submissions in a NSFW channel!")
                }
            } else {
                event.message.reply("${CustomEmote.XMARK} I was unable to grab an image from `$multiredditName`! (Ran out of options)")
            }
        } catch (e: NetworkException) {
            event.message.reply("${CustomEmote.XMARK} I was unable to grab an image from `$multiredditName`! (Network error)")
        } catch (e: ApiException) {
            event.message.reply("${CustomEmote.XMARK} I was unable to grab an image from `$multiredditName`! (Private subreddit?)")
        } catch (e: JsonDataException) {
            event.message.reply("${CustomEmote.XMARK} I was unable to grab an image from `$multiredditName`! (No such subreddit?)")
        } catch (e: NullPointerException) {
            event.message.reply("${CustomEmote.XMARK} I was unable to grab an image from `$multiredditName`! (No such subreddit?)")
        }
    }

    private fun getRandomImage(multireddit: SubredditReference): Submission? {
        val imageQueue: Queue<Submission> = imageCache.getOrDefault(multireddit.subreddit, LinkedList<Submission>())
        if (imageQueue.isEmpty()) {
            multireddit.posts().sorting(SubredditSort.HOT).build().accumulateMerged(1).filter {
                it.url.contains(Regex(".(jpg|png|jpeg|gif|webp)$"))
            }.shuffled().forEach {
                imageQueue.offer(it)
            }
            imageCache[multireddit.subreddit] = imageQueue
        }
        return imageQueue.poll()
    }
}
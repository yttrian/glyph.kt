/*
 * RedditSkill.kt
 *
 * Glyph, a Discord bot that uses natural language instead of commands
 * powered by DialogFlow and Kotlin
 *
 * Copyright (C) 2017-2021 by Ian Moore
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

package org.yttr.glyph.bot.skills.play

import com.squareup.moshi.JsonDataException
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
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.jodah.expiringmap.ExpiringMap
import org.yttr.glyph.bot.Glyph
import org.yttr.glyph.bot.ai.AIResponse
import org.yttr.glyph.bot.messaging.Response
import org.yttr.glyph.bot.skills.Skill
import java.time.Instant
import java.util.LinkedList
import java.util.Queue
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * A skill that attempts to show users an image from a subreddit
 */
class RedditSkill : Skill("skill.reddit") {
    private val conf = Glyph.conf.getConfig("skills.reddit")
    private val client: RedditClient = OAuthHelper.automatic(
        OkHttpNetworkAdapter(
            UserAgent(
                "discord",
                this.javaClass.canonicalName,
                Glyph.version,
                conf.getString("username")
            )
        ),
        Credentials.userless(
            conf.getString("client-id"),
            conf.getString("client-secret"),
            UUID.randomUUID()
        )
    ).apply { logger = NoopHttpLogger() }
    private val imageCache: MutableMap<String, Queue<Submission>> =
        ExpiringMap.builder().maxSize(CACHE_SIZE).expiration(CACHE_TTL_MINUTES, TimeUnit.MINUTES).build()

    override suspend fun onTrigger(event: MessageReceivedEvent, ai: AIResponse): Response {
        // Send typing since this can take some time and we want to indicate we are paying attention
        event.channel.sendTyping().queue()
        // Try to get the multireddit name
        val multiredditName: String = ai.result.getStringParameter("multireddit")
            ?: return Response.Volatile("I did not understand what subreddit you were asking for!")
        // If we have a multireddit name, try getting the reference to it otherwise report the failure
        return try {
            val subreddit: SubredditReference = client.subreddit(multiredditName)
            val submission: Submission? = getRandomImage(subreddit)
            // If we were actually able to grab an image, send it, if allowed
            if (submission != null) {
                val nsfwAllowed = if (event.channelType.isGuild) event.textChannel.isNSFW else false
                if ((submission.isNsfw && nsfwAllowed) || !submission.isNsfw) {
                    Response.Volatile(
                        EmbedBuilder()
                            .setTitle(submission.title, "https://reddit.com${submission.permalink}")
                            .setImage(submission.url)
                            .setFooter("r/${submission.subreddit}", null)
                            .setTimestamp(Instant.now())
                            .build()
                    )
                } else {
                    Response.Volatile("I can only show NSFW submissions in a NSFW channel!")
                }
            } else {
                whine(multiredditName, "Ran out of options")
            }
        } catch (e: NetworkException) {
            whine(multiredditName, "Network error", e)
        } catch (e: ApiException) {
            whine(multiredditName, "Private subreddit?", e)
        } catch (e: JsonDataException) {
            whine(multiredditName, "No such subreddit?", e)
        } catch (e: NullPointerException) {
            whine(multiredditName, "No such subreddit?", e)
        }
    }

    private fun whine(multiredditName: String, likelyCause: String, throwable: Throwable? = null): Response.Volatile {
        if (throwable !== null) {
            log.debug(likelyCause, throwable)
        }
        return Response.Volatile("I was unable to grab an image from `$multiredditName`! ($likelyCause)")
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

    companion object {
        private const val CACHE_SIZE: Int = 10
        private const val CACHE_TTL_MINUTES: Long = 30
    }
}

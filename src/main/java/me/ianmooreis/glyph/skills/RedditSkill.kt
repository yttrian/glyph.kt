package me.ianmooreis.glyph.skills

import ai.api.model.AIResponse
import com.squareup.moshi.JsonDataException
import me.ianmooreis.glyph.Glyph
import me.ianmooreis.glyph.extensions.random
import me.ianmooreis.glyph.extensions.reply
import me.ianmooreis.glyph.orchestrators.messaging.CustomEmote
import me.ianmooreis.glyph.orchestrators.skills.SkillAdapter
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
import java.time.Instant
import java.util.*


object RedditSkill : SkillAdapter("skill.reddit") {
    private val client: RedditClient = OAuthHelper.automatic(
            OkHttpNetworkAdapter(UserAgent("discord", this.javaClass.simpleName, Glyph.version, "IanM_56")),
            Credentials.userless(System.getenv("REDDIT_CLIENT_ID"), System.getenv("REDDIT_CLIENT_SECRET"), UUID.randomUUID()))
    private val imageCache = mutableMapOf<String, List<Submission>>()
    init {
        this.client.logger = NoopHttpLogger()
    }

    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        // Try to get the multireddit name
        val multiredditName: String? = try {
            ai.result.getStringParameter("multireddit").replace("\\", "")
        } catch (e: IllegalStateException) { null }  // Sometimes it doesn't parse the string right
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
                    event.message.reply(EmbedBuilder()
                            .setTitle(submission.title, "https://reddit.com${submission.permalink}")
                            .setImage(submission.url)
                            .setFooter("r/${submission.subreddit}", null)
                            .setTimestamp(Instant.now())
                            .build())
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
        val imagePosts = imageCache.getOrPut(multireddit.subreddit) {
            multireddit.posts().sorting(SubredditSort.HOT).build().accumulateMerged(2).filter {
                it.url.contains(Regex(".(jpg|png|jpeg|gif|webp)$"))
            }
        }
        return imagePosts.random()
    }
}
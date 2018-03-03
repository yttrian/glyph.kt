package me.ianmooreis.glyph.skills

import ai.api.model.AIResponse
import com.squareup.moshi.JsonDataException
import me.ianmooreis.glyph.Glyph
import me.ianmooreis.glyph.orchestrators.CustomEmote
import me.ianmooreis.glyph.orchestrators.Skill
import me.ianmooreis.glyph.orchestrators.reply
import net.dean.jraw.ApiException
import net.dean.jraw.RedditClient
import net.dean.jraw.http.NetworkException
import net.dean.jraw.http.NoopHttpLogger
import net.dean.jraw.http.OkHttpNetworkAdapter
import net.dean.jraw.http.UserAgent
import net.dean.jraw.models.Submission
import net.dean.jraw.oauth.Credentials
import net.dean.jraw.oauth.OAuthHelper
import net.dean.jraw.pagination.DefaultPaginator
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.time.Instant
import java.util.*


object RedditSkill : Skill("skill.reddit") {
    private val client: RedditClient = OAuthHelper.automatic(
            OkHttpNetworkAdapter(UserAgent("discord", this.javaClass.simpleName, Glyph.version, "IanM_56")),
            Credentials.userless(System.getenv("REDDIT_CLIENT_ID"), System.getenv("REDDIT_CLIENT_SECRET"), UUID.randomUUID()))
    private var paginatorCache = mutableMapOf<String, DefaultPaginator<Submission>>()
    private var submissionCache = mutableMapOf<String, Iterator<Submission>>()

    init {
        this.client.logger = NoopHttpLogger()
    }

    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        val subredditName = ai.result.getStringParameter("multireddit").replace("\\", "")
        try {
            val subreddit = this.client.subreddit(subredditName)
            val paginator = this.paginatorCache.getOrPut(subreddit.subreddit) { client.subreddit(subreddit.subreddit).posts().build() }
            var submissions = this.submissionCache.getOrPut(subreddit.subreddit) { paginator.next().filter {
                it.preview != null
            }.listIterator() }
            if (!submissions.hasNext()) {
                submissions = paginator.next().filter { it.url.endsWith(".png") }.listIterator()
                this.submissionCache[subreddit.subreddit] = submissions
            }
            val submission = submissions.next()
            val nsfwAllowed = if (event.channelType.isGuild) event.textChannel.isNSFW else false
            if ((submission.isNsfw && nsfwAllowed) || !submission.isNsfw) {
                event.message.reply(EmbedBuilder()
                        .setTitle(submission.title, "https://reddit.com${submission.permalink}")
                        .setImage(submission.url)
                        .setFooter("r/${submission.subreddit}", null)
                        .setTimestamp(Instant.now())
                        .build())
            } else {
                event.message.reply("${CustomEmote.EXPLICIT} I can only show NSFW submissions in a NSFW channel!")
            }
        } catch (e: NetworkException) {
            event.message.reply("${CustomEmote.GRIMACE} I was unable to grab an image from `$subredditName`! (Network error)")
        } catch (e: ApiException) {
            event.message.reply("${CustomEmote.CONFIDENTIAL} I was unable to grab an image from `$subredditName`! (Private subreddit?)")
        } catch (e: NoSuchElementException) {
            event.message.reply("${CustomEmote.GRIMACE} I was unable to grab an image from `$subredditName`! (Ran out of options)")
        } catch (e: JsonDataException) {
            event.message.reply("${CustomEmote.THINKING} I was unable to grab an image from `$subredditName`! (No such subreddit)")
        } catch (e: NullPointerException) {
            event.message.reply("${CustomEmote.THINKING} I was unable to grab an image from `$subredditName`! (No such subreddit)")
        }
    }
}
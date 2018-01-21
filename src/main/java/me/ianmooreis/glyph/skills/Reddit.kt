package me.ianmooreis.glyph.skills

import ai.api.model.AIResponse
import me.ianmooreis.glyph.orchestrators.Skill
import me.ianmooreis.glyph.orchestrators.reply
import net.dean.jraw.RedditClient
import net.dean.jraw.http.NetworkException
import net.dean.jraw.http.NoopHttpLogger
import net.dean.jraw.http.OkHttpNetworkAdapter
import net.dean.jraw.http.UserAgent
import net.dean.jraw.models.Submission
import net.dean.jraw.oauth.Credentials
import net.dean.jraw.oauth.OAuthHelper
import net.dean.jraw.pagination.DefaultPaginator
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.util.*


object RedditSkill : Skill("skill.reddit") {
    private val client: RedditClient = OAuthHelper.automatic(
            OkHttpNetworkAdapter(UserAgent("discord", this.javaClass.simpleName, "v0.1", "IanM_56")),
            Credentials.userless(System.getenv("REDDIT_CLIENT_ID"), System.getenv("REDDIT_CLIENT_SECRET"), UUID.randomUUID()))
    private var paginatorCache = mutableMapOf<String, DefaultPaginator<Submission>>()
    private var submissionCache = mutableMapOf<String, Iterator<Submission>>()

    init {
        this.client.logger = NoopHttpLogger()
    }

    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        val subredditName = ai.result.getStringParameter("multireddit").replace("\\", "") //TODO: Investigate escape characters going to Dialogflow
        try {
            val subreddit = this.client.subreddit(subredditName).subreddit
            val paginator = this.paginatorCache.getOrPut(subreddit) { client.subreddit(subreddit).posts().build() }
            var submissions = this.submissionCache.getOrPut(subreddit) { paginator.next().filter { it.url.endsWith(".png") }.listIterator() }
            if (!submissions.hasNext()) {
                submissions = paginator.next().filter { it.url.endsWith(".png") }.listIterator()
                this.submissionCache[subreddit] = submissions
            }
            event.message.reply(submissions.next().url)
        } catch (e: NetworkException) {
            event.message.reply("I was unable to grab an image from `$subredditName`")
        }
    }
}
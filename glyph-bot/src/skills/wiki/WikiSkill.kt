package org.yttr.glyph.bot.skills.wiki

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.yttr.glyph.bot.ai.AIResponse
import org.yttr.glyph.bot.messaging.Response
import org.yttr.glyph.bot.skills.Skill
import org.yttr.glyph.shared.config.server.WikiConfig
import java.time.Instant

/**
 * A skill that allows users to search for stuff across multiple wikis
 */
class WikiSkill : Skill("skill.wiki") {
    override suspend fun onTrigger(event: MessageReceivedEvent, ai: AIResponse): Response {
        val query: String = ai.result.getStringParameter("search_query") ?: ""
        val config: WikiConfig = if (event.isFromGuild) event.guild.config.wiki else defaultConfig.wiki
        val requestedSource: String? = ai.result.getStringParameter("fandom_wiki")
        val sources: List<String> = if (requestedSource != null) {
            listOf(requestedSource)
        } else {
            (config.sources + "wikipedia")
        }.distinctBy { it.lowercase() }

        event.channel.sendTyping().queue()
        sources.forEach { source ->
            val article: WikiArticle? = if (source.equals("wikipedia", true)) {
                WikipediaExtractor().getArticle(query)
            } else {
                FandomExtractor(source, config.minimumQuality).getArticle(query)
            }
            if (article != null) {
                return Response.Volatile(
                    EmbedBuilder()
                        .setTitle(article.title, article.url)
                        .setDescription(article.abstract)
                        .setThumbnail(article.thumbnail)
                        .setFooter(sourceDisplay(source), null)
                        .setTimestamp(Instant.now())
                        .build()
                )
            }
        }

        val sourcesDisplay = sources.mapIndexed { index, source ->
            (if (sources.size > 1 && (index + 1) == sources.size) "or " else "") + sourceDisplay(source)
        }.joinToString()
        return Response.Volatile("No results found for `$query` on $sourcesDisplay!")
    }

    private fun sourceDisplay(name: String): String =
        if (name.equals("wikipedia", true)) "Wikipedia" else "$name wiki"
}

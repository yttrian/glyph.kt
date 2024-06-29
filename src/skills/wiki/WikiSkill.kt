package org.yttr.glyph.skills.wiki

import dev.kord.core.event.message.MessageCreateEvent
import net.dv8tion.jda.api.EmbedBuilder
import org.yttr.glyph.ai.AIResponse
import org.yttr.glyph.config.server.WikiConfig
import org.yttr.glyph.directors.Skill
import org.yttr.glyph.directors.nlp.Skill
import org.yttr.glyph.messaging.Response
import java.time.Instant

/**
 * A skill that allows users to search for stuff across multiple wikis
 */
object WikiSkill : Skill("skill.wiki") {
    override suspend fun perform(event: MessageCreateEvent, ai: AIResponse) {
        val query: String = ai.result.getStringParameter("search_query") ?: ""
        val config: WikiConfig = if (event.isFromGuild) event.guild.config.wiki else defaultConfig.wiki
        val requestedSource: String? = ai.result.getStringParameter("fandom_wiki")
        val sources: List<String> = if (requestedSource != null) {
            listOf(requestedSource)
        } else {
            (config.sources + "wikipedia")
        }.distinctBy { it.toLowerCase() }

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

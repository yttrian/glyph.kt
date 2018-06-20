package me.ianmooreis.glyph.skills.wiki

import ai.api.model.AIResponse
import me.ianmooreis.glyph.configs.WikiConfig
import me.ianmooreis.glyph.extensions.config
import me.ianmooreis.glyph.extensions.reply
import me.ianmooreis.glyph.orchestrators.DatabaseOrchestrator
import me.ianmooreis.glyph.orchestrators.skills.Skill
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.net.URL
import java.time.Instant

/**
 * A skill that allows users to search for stuff across multiple wikis
 */
object WikiSkill : Skill("skill.wiki") {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        val query: String = ai.result.getStringParameter("search_query")
        val config: WikiConfig = event.guild?.config?.wiki ?: DatabaseOrchestrator.getDefaultServerConfig().wiki
        val requestedSource: String? = ai.result.getStringParameter("fandom_wiki", null)?.trim()
        val sources: List<String> = if (requestedSource != null) listOf(requestedSource) else config.sources.filterNotNull()
        val sourcesDisplay = sources.map { if (it.toLowerCase() == "wikipedia") "Wikipedia" else "$it wiki" }
        event.channel.sendTyping().queue()
        sources.forEachIndexed { index, source ->
            val article: WikiArticle? = if (source.toLowerCase() == "wikipedia") {
                WikipediaExtractor.getArticle(query)
            } else {
                FandomExtractor.getArticle(source, query, config.minimumQuality)
            }
            if (article != null) {
                event.message.reply(getResultEmbed(article.title, article.url, article.intro, sourcesDisplay[index]))
                return
            }
        }
        event.message.reply("No results found for `$query` on ${sourcesDisplay.joinToString()}!")
    }

    private fun getResultEmbed(title: String, url: URL, description: String, wiki: String): MessageEmbed {
        return EmbedBuilder()
            .setTitle(title, url.toString())
            .setDescription(description)
            .setFooter(wiki, null)
            .setTimestamp(Instant.now())
            .build()
    }
}
package me.ianmooreis.glyph.skills

import ai.api.model.AIResponse
import me.ianmooreis.glyph.extensions.config
import me.ianmooreis.glyph.extensions.reply
import me.ianmooreis.glyph.orchestrators.DatabaseOrchestrator
import me.ianmooreis.glyph.orchestrators.Skill
import me.ianmooreis.glyph.utils.libraries.FandomExtractor
import me.ianmooreis.glyph.utils.libraries.WikipediaExtractor
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.net.URL
import java.time.Instant

object WikiSkill : Skill("skill.wiki") {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        val query = ai.result.getStringParameter("search_query")
        val config = event.guild?.config?.wiki ?: DatabaseOrchestrator.getDefaultServerConfig().wiki
        val defaultWiki = config.source
        val wiki = ai.result.getStringParameter("fandom_wiki", defaultWiki).trim()
        if (wiki.toLowerCase() == "wikipedia") {
            WikipediaExtractor.getPage(query, {event.message.reply("No results found for `$query` on Wikipedia!")}) {
                event.message.reply(getResultEmbed(it.title, it.url, it.intro, "Wikipedia"))
            }
        } else {
            FandomExtractor.getPage(wiki, query, config.minimumQuality, {event.message.reply("No results found for `$query` on $wiki!")}) {
                event.message.reply(getResultEmbed(it.title, it.url, it.intro, wiki))
            }
        }
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
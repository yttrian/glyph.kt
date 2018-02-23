package me.ianmooreis.glyph.skills

import ai.api.model.AIResponse
import me.ianmooreis.glyph.orchestrators.DatabaseOrchestrator
import me.ianmooreis.glyph.orchestrators.Skill
import me.ianmooreis.glyph.orchestrators.config
import me.ianmooreis.glyph.orchestrators.reply
import me.ianmooreis.glyph.utils.libraries.FandomExtractor
import me.ianmooreis.glyph.utils.libraries.WikipediaExtractor
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.time.Instant

object WikiSkill : Skill("skill.wiki") {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        val query = ai.result.getStringParameter("search_query")
        val wiki = event.guild?.config?.wiki ?: DatabaseOrchestrator.getDeafultServerConfig().wiki
        if (wiki.toLowerCase() == "wikipedia") {
            val page: WikipediaExtractor.WikipediaPage? = WikipediaExtractor.getPage(query)
            if (page != null) {
                event.message.reply(EmbedBuilder()
                        .setTitle(page.title, page.url.toString())
                        .setDescription(page.intro)
                        .setFooter("Wikipedia", null)
                        .setTimestamp(Instant.now())
                        .build())
            } else {
                event.message.reply("No results found for `$query`!")
            }
        } else {
            val page: FandomExtractor.FandomPage? = FandomExtractor.getPage(wiki, query)
            if (page != null) {
                event.message.reply(EmbedBuilder()
                        .setTitle(page.title, page.url.toString())
                        .setDescription(page.intro)
                        .setFooter(wiki, null)
                        .setTimestamp(Instant.now())
                        .build())
            } else {
                event.message.reply("No results found for `$query`!")
            }
        }
    }
}
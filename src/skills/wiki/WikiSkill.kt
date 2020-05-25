/*
 * WikiSkill.kt
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

package me.ianmooreis.glyph.skills.wiki

import me.ianmooreis.glyph.ai.AIResponse
import me.ianmooreis.glyph.directors.config.server.WikiConfig
import me.ianmooreis.glyph.directors.skills.Skill
import me.ianmooreis.glyph.extensions.config
import me.ianmooreis.glyph.messaging.Response
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.time.Instant

/**
 * A skill that allows users to search for stuff across multiple wikis
 */
class WikiSkill : Skill("skill.wiki") {
    override suspend fun onTrigger(event: MessageReceivedEvent, ai: AIResponse): Response {
        val query: String = ai.result.getStringParameter("search_query") ?: ""
        val config: WikiConfig = event.guild.config.wiki
        val requestedSource: String? = ai.result.getStringParameter("fandom_wiki")
        val sources: List<String> =
            if (requestedSource != null) listOf(requestedSource) else (config.sources + "wikipedia")
        event.channel.sendTyping().queue()
        sources.forEachIndexed { index, source ->
            val article: WikiArticle? = if (source.toLowerCase() == "wikipedia") {
                WikipediaExtractor().getArticle(query)
            } else {
                FandomExtractor(source, config.minimumQuality).getArticle(query)
            }
            if (article != null) {
                return Response.Volatile(
                    EmbedBuilder()
                        .setTitle(article.title, article.url)
                        .setDescription(article.intro)
                        .setFooter(sourceDisplay(source), null)
                        .setTimestamp(Instant.now())
                        .build()
                )
            }
        }
        val sourcesDisplay = sources.joinToString { sourceDisplay(it) }
        return Response.Volatile("No results found for `$query` on $sourcesDisplay!")
    }

    private fun sourceDisplay(name: String): String = if (name.equals("wikipedia", true)) "Wikipedia" else "$name wiki"
}
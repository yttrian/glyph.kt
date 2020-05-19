/*
 * WikipediaExtractor.kt
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

import io.ktor.client.features.ResponseException
import io.ktor.client.request.get
import io.ktor.http.URLBuilder
import io.ktor.http.takeFrom

/**
 * Grabs articles from Wikipedia
 */
class WikipediaExtractor : WikiExtractor() {
    //private val log: Logger = LoggerFactory.getLogger(this.javaClass.simpleName)

    /**
     * Tries to find an article from a search
     *
     * @param query the search query
     */
    override suspend fun getArticle(query: String): WikiArticle? {
        val queryUrl = URLBuilder().takeFrom("https://en.wikipedia.org/w/api.php").apply {
            parameters.apply {
                append("action", "query")
                append("format", "json")
                append("prop", "extracts|info")
                append("titles", query)
                append("redirects", "1")
                append("explaintext", "1")
                append("inprop", "url")
                append("exchars", "500")
            }
        }.build()

        data class WikipediaPage(val title: String, val extract: String, val fullurl: String)
        data class WikipediaQuery(val pages: List<WikipediaPage>)
        data class WikipediaResult(val query: WikipediaQuery)

        return try {
            val result = client.get<WikipediaResult>(queryUrl)

            result.query.pages.firstOrNull()?.let {
                WikiArticle(it.title, it.extract, it.fullurl)
            }
        } catch (e: ResponseException) {
            null
        }
    }
}


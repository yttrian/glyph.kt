/*
 * FandomExtractor.kt
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
import io.ktor.http.encodeURLPath
import io.ktor.http.takeFrom

/**
 * Grabs articles from Fandom wikis
 */
class FandomExtractor(
    /**
     * The Fandom wiki to search
     */
    val wiki: String,
    /**
     * The minimum acceptable article quality
     */
    val minimumQuality: Int
) : WikiExtractor() {
    private val apiBase = "https://${wiki.encodeURLPath()}.fandom.com/api/v1"

    /**
     * Tries to grab an article from a search on a wiki
     *
     * @param query the search query
     */
    override suspend fun getArticle(query: String): WikiArticle? {
        val searchUrl = URLBuilder("$apiBase/Search/List").apply {
            parameters.apply {
                append("limit", "1")
                append("minArticleQuality", minimumQuality.toString())
                append("batch", "1")
                append("namespaces", "0%2C14")
            }
        }.build()

        data class FandomSearchItem(val id: String)
        data class FandomSearchResult(val items: List<FandomSearchItem>)
        data class FandomContent(val text: String)
        data class FandomSections(val content: List<FandomContent>)
        data class FandomPage(
            val title: String,
            val url: String,
            val snippet: String,
            val sections: List<FandomSections>
        )

        return try {
            val searchResult = client.get<FandomSearchResult>(searchUrl)

            searchResult.items.firstOrNull()?.let {
                val pageUrl = URLBuilder().takeFrom("$apiBase/Articles/AsSimpleJson").apply {
                    parameters.append("id", it.id)
                }.build()

                val pageResult = client.get<FandomPage>(pageUrl)
                val snippet = pageResult.sections.firstOrNull()?.content?.firstOrNull()?.text ?: pageResult.snippet

                WikiArticle(pageResult.title, snippet, pageResult.url)
            }
        } catch (e: ResponseException) {
            null
        }
    }
}
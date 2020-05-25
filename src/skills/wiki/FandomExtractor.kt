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

    companion object {
        /**
         * http://community.wikia.com/wiki/Help:Namespaces
         */
        const val NAMESPACES: String = "0,14"

        /**
         * The maximum length Fandom allows asking for
         */
        const val MAX_ABSTRACT_LENGTH: String = "500"

        /**
         * Represents an item from a Fandom search
         */
        data class SearchResult(
            /**
             * Identity of search result
             */
            val id: String
        )

        /**
         * Represents a Fandom search result listing
         */
        data class SearchListing(
            /**
             * Search result items
             */
            val items: List<SearchResult>
        )

        /**
         * Represents a Fandom page
         */
        data class Page(
            /**
             * Title of the page
             */
            val title: String,
            /**
             * URL of the page
             */
            val url: String,
            /**
             * Abstract of the page
             */
            val abstract: String,
            /**
             * URL of the thumbnail for the page
             */
            val thumbnail: String
        )

        /**
         * Represents the result of a details listing
         */
        data class DetailsListing(
            /**
             * Detail result items
             */
            val items: Map<String, Page>
        )
    }

    /**
     * Tries to grab an article from a search on a wiki
     *
     * @param query the search query
     */
    override suspend fun getArticle(query: String): WikiArticle? = try {
        val searchUrl = URLBuilder("$apiBase/Search/List").apply {
            parameters.apply {
                append("query", query)
                append("limit", "1")
                append("minArticleQuality", minimumQuality.toString())
                append("batch", "1")
                append("namespaces", NAMESPACES)
            }
        }.build()

        val searchResult = client.get<SearchListing>(searchUrl)

        searchResult.items.firstOrNull()?.let {
            val pageUrl = URLBuilder().takeFrom("$apiBase/Articles/Details").apply {
                parameters.apply {
                    append("ids", it.id)
                    append("abstract", MAX_ABSTRACT_LENGTH)
                }
            }.build()

            val page = client.get<DetailsListing>(pageUrl).items[it.id]

            if (page != null) WikiArticle(page.title, page.abstract, page.url, page.thumbnail) else null
        }
    } catch (e: ResponseException) {
        null
    }
}
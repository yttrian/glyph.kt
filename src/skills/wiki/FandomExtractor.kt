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
import io.ktor.client.request.parameter
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
    private val minimumQuality: Int
) : WikiExtractor() {
    private val urlBase = "https://${wiki.encodeURLPath()}.fandom.com"
    private val apiBase = "$urlBase/api/v1"

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
        val searchResult = client.get<SearchListing> {
            url.takeFrom("$apiBase/Search/List")
            parameter("query", query)
            parameter("limit", "1")
            parameter("minArticleQuality", minimumQuality.toString())
            parameter("batch", "1")
            parameter("namespaces", NAMESPACES)
            println(url.buildString())
        }

        searchResult.items.firstOrNull()?.let {
            val page = client.get<DetailsListing> {
                url.takeFrom("$apiBase/Articles/Details")
                parameter("ids", it.id)
                parameter("abstract", MAX_ABSTRACT_LENGTH)
            }.items[it.id]

            if (page != null) WikiArticle(
                page.title,
                page.abstract,
                urlBase + page.url,
                page.thumbnail
            ) else null
        }
    } catch (e: ResponseException) {
        e.printStackTrace()
        null
    }
}
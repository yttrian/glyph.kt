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

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL
import java.net.URLEncoder

/**
 * Grabs articles from Fandom wikis
 */
object FandomExtractor {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass.simpleName)

    /**
     * Tries to grab an article from a search on a wiki
     *
     * @param wiki the wiki to search
     * @param query the search query
     * @param minimumQuality the minimum article quality to accept
     */
    fun getArticle(wiki: String, query: String, minimumQuality: Int): WikiArticle? {
        val searchUrl = "https://${URLEncoder.encode(wiki, "UTF-8")}.wikia.com/" +
                "api/v1/Search/List?query=${URLEncoder.encode(query, "UTF-8")}" +
                "&limit=1&minArticleQuality=${URLEncoder.encode(
                    minimumQuality.toString(),
                    "UTF-8"
                )}&batch=1&namespaces=0%2C14"
        val (_, _, searchResult) = searchUrl.httpGet().responseString()
        return when (searchResult) {
            is Result.Success -> {
                val page = JSONObject(searchResult.get()).getJSONArray("items").getJSONObject(0)
                val pageUrl = "http://$wiki.wikia.com/api/v1/Articles/AsSimpleJson?id=${page.getInt("id")}"
                val (_, pageResponse, pageResult) = pageUrl.httpGet().responseString()
                val snippet = if (pageResponse.statusCode == 200) {
                    JSONObject(pageResult.get()).getJSONArray("sections").getJSONObject(0)
                        .getJSONArray("content").getJSONObject(0)
                        .getString("text")
                } else {
                    page.getString("snippet")
                }

                WikiArticle(page.getString("title"), snippet, URL(page.getString("url")))
            }
            is Result.Failure -> {
                log.info("Failed to find page for query $query!")
                null
            }
        }
    }
}
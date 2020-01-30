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

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import org.json.JSONException
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.simple.SimpleLoggerFactory
import java.net.URL
import java.net.URLEncoder

/**
 * Grabs articles from Wikipedia
 */
object WikipediaExtractor {
    private val log: Logger = SimpleLoggerFactory().getLogger(this.javaClass.simpleName)

    /**
     * Tries to find an article from a search
     *
     * @param query the search query
     */
    fun getArticle(query: String): WikiArticle? {
        val queryUrl =
            "https://en.wikipedia.org/w/api.php?action=query&format=json&prop=extracts%7Cinfo&titles=${URLEncoder.encode(
                query,
                "UTF-8"
            )}&redirects=1&exintro=1&explaintext=1&inprop=url&exchars=500"
        val (_, _, result) = queryUrl.httpGet().responseString()
        return when (result) {
            is Result.Success -> {
                try {
                    val pages = JSONObject(result.get()).getJSONObject("query").getJSONObject("pages")
                    val page = pages.getJSONObject(pages.keys().next())
                    val extract = page.getString("extract")
                    if (extract != null && !extract.contains("may refer to")) {
                        WikiArticle(page.getString("title"), extract, URL(page.getString("fullurl")))
                    } else {
                        null
                    }
                } catch (e: JSONException) {
                    null
                }
            }
            is Result.Failure -> {
                log.debug("Failed to find page for query $query!")
                null
            }
        }
    }
}


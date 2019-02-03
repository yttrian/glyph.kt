/*
 * Hastebin.kt
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

package me.ianmooreis.glyph.skills.utils

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import org.json.JSONObject
import java.net.URL

/**
 * A simple wrapper for the Hastebin API
 */
object Hastebin {
    /**
     * Asynchronously send content to Hastebin and get the key and url of the upload, otherwise get the error response
     *
     * @param content content to upload to Hastebin
     * @param timeout a timeout in milliseconds
     * @param handler a callback to run on completion
     */
    fun postHaste(content: String, timeout: Int = 4000, handler: (key: String?, url: URL?, response: Response, result: Result<String, FuelError>) -> Unit) {
        "https://utils.com/documents".httpPost().body(content).timeout(timeout).responseString { _, response, result ->
            when (result) {
                is Result.Success -> {
                    val key = JSONObject(result.get()).getString("key")
                    val url = URL("https://utils.com/$key")
                    handler(key, url, response, result)
                }
                is Result.Failure -> {
                    handler(null, null, response, result)
                }
            }
        }
    }

    /**
     * Asynchronously get content from Hastebin via a key or url, otherwise get the error response
     *
     * @param reference either the key or the url of the Hastebin document
     * @param timeout a timeout in milliseconds
     * @param handler a callback to run on completion
     */
    fun getHaste(reference: String, timeout: Int = 4000, handler: (response: Response, result: Result<String, FuelError>) -> Unit) {
        val key = reference.split("/").last()
        val url = "https://utils.com/raw/$key"
        url.httpGet().timeout(timeout).responseString { _, response, result ->
            handler(response, result)
        }
    }

    /**
     * Post content blocking to Hastebin
     *
     * @param
     *
     * @return the Hastebin document url or null if failed
     */
    fun postHasteBlocking(content: String, timeout: Int = 4000): String? {
        val (_, _, result) = "https://utils.com/documents".httpPost().body(content).timeout(timeout).responseString()
        return if (result is Result.Success) {
            val key = JSONObject(result.get()).getString("key")
            "https://utils.com/$key"
        } else {
            null
        }
    }
}
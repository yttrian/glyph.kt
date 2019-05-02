/*
 * Myjson.kt
 *
 * Glyph, a Discord bot that uses natural language instead of commands
 * powered by DialogFlow and Kotlin
 *
 * Copyright (C) 2017-2019 by Ian Moore
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
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import org.json.JSONObject

/**
 * A simple wrapper for the Myjson API
 */
object Myjson {
    private const val apiRootUrl = "https://api.myjson.com/bins/"

    /**
     * Uploads a JSON to Myjson
     *
     * @param json the json the upload
     * @param timeout maximum time to wait for a connection
     * @param handler a function to asynchronously handle the resulting key
     */
    fun postJSON(
        json: String,
        timeout: Int = 4000,
        handler: (key: String?) -> Unit
    ) {
        apiRootUrl.httpPost().header("Content-Type" to "application/json").body(json).timeout(timeout)
            .responseString { _, _, result ->
                val key = when (result) {
                    is Result.Success -> {
                        JSONObject(result.get()).getString("uri").substringAfterLast('/')
                    }
                    is Result.Failure -> {
                        null
                    }
                }
                handler(key)
            }
    }

    /**
     * Retrieves a JSON from Myjson
     *
     * @param key the bin key
     * @param timeout maximum time to wait for a connection
     * @param handler a function to asynchronously handle the result
     */
    fun getJSON(
        key: String,
        timeout: Int = 4000,
        handler: (result: Result<String, FuelError>) -> Unit
    ) {
        val url = apiRootUrl + key
        url.httpGet().timeout(timeout).responseString { _, _, result ->
            handler(result)
        }
    }
}
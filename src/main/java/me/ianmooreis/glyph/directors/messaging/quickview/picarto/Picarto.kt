/*
 * Picarto.kt
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

package me.ianmooreis.glyph.directors.messaging.quickview.picarto

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import me.ianmooreis.glyph.extensions.contentClean
import me.ianmooreis.glyph.extensions.reply
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import org.slf4j.Logger
import org.slf4j.simple.SimpleLoggerFactory

/**
 * Handles the creation of QuickViews for picarto.tv links
 */
object Picarto {
    private val log: Logger = SimpleLoggerFactory().getLogger(this.javaClass.simpleName)
    private val urlFormat = Regex("((http[s]?)://)?(www.)?(picarto.tv)/(\\w*)/?", RegexOption.IGNORE_CASE)

    /**
     * Makes any QuickViews for links found in a message
     *
     * @param event the message event
     */
    fun makeQuickviews(event: MessageReceivedEvent) {
        urlFormat.findAll(event.message.contentClean)
            .map { getChannel(it.groups[5]!!.value) }
            .forEach {
                if (it != null) {
                    event.message.reply(it.getEmbed())
                    log.info("Created picarto QuickView in ${event.guild} for ${it.name}")
                }
            }
    }

    private fun getChannel(name: String): Channel? { //TODO: Figure out how not to do it blocking, because async had errors
        val (_, _, result) = "https://api.picarto.tv/v1/channel/name/$name".httpGet().responseString()
        return when (result) {
            is Result.Success -> {
                Gson().fromJson(result.get(), Channel::class.java)
            }
            is Result.Failure -> {
                log.warn("Failed to get channel $name from picarto!")
                return null
            }
        }
    }
}
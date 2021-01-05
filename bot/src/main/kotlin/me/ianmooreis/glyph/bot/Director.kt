/*
 * Director.kt
 *
 * Glyph, a Discord bot that uses natural language instead of commands
 * powered by DialogFlow and Kotlin
 *
 * Copyright (C) 2017-2020 by Ian Moore
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

package me.ianmooreis.glyph.bot

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import me.ianmooreis.glyph.bot.directors.config.ConfigDirector
import me.ianmooreis.glyph.shared.config.server.ServerConfig
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.apache.commons.codec.digest.DigestUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.coroutines.CoroutineContext

/**
 * The definition of a director, with pre-included properties like a logger
 */
abstract class Director : ListenerAdapter(), CoroutineScope {
    /**
     * The directors's logger which will show the director's name in the console when logs are made
     */
    protected val log: Logger = LoggerFactory.getLogger(this.javaClass.simpleName)

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + SupervisorJob()

    lateinit var configDirector: ConfigDirector

    /**
     * md5hex of the author id concatenated with the channel id to identify a context without being too revealing
     */
    protected val MessageReceivedEvent.contextHash: String
        get() = DigestUtils.md5Hex(author.id + channel.id)

    protected val Guild.config: ServerConfig
        get(): ServerConfig = configDirector.getServerConfig(this)
}

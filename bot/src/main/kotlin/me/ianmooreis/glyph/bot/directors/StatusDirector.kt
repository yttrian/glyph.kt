/*
 * StatusDirector.kt
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

package me.ianmooreis.glyph.bot.directors

import com.google.gson.JsonParser
import me.ianmooreis.glyph.bot.Director
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.ReadyEvent
import java.util.*
import kotlin.concurrent.schedule

/**
 * Manages the status messages of the client
 */
object StatusDirector : Director() {
    private val statuses: List<Activity> by lazy {
        val configFile = this.javaClass.classLoader.getResourceAsStream("activities.json")?.reader()
        val config = JsonParser().parse(configFile).asJsonObject
        val types = enumValues<Activity.ActivityType>()

        types.flatMap { type ->
            config.getAsJsonArray(type.name.toLowerCase())?.map {
                Activity.of(type, it.asString)
            } ?: emptyList()
        }
    }

    /**
     * When the client is ready
     */
    override fun onReady(event: ReadyEvent) {
        log.info("Ready on shard ${event.jda.shardInfo.shardId}/${event.jda.shardInfo.shardTotal} with ${event.jda.guilds.count()} guilds")

        // Automatically update the status every hour
        val hour = 3.6e+6.toLong()
        Timer().schedule(0, hour) {
            event.jda.presence.setPresence(OnlineStatus.ONLINE, getRandomStatus())
        }
    }

    /**
     * Change the presence of the client
     */
    fun setPresence(jda: JDA, status: OnlineStatus = jda.presence.status, game: Activity? = jda.presence.activity) {
        jda.presence.setPresence(status, game)
    }

    private fun getRandomStatus(): Activity = statuses.random()
}
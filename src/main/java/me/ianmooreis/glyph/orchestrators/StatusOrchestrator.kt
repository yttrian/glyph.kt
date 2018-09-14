/*
 * StatusOrchestrator.kt
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

package me.ianmooreis.glyph.orchestrators

import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.entities.Game
import net.dv8tion.jda.core.events.ReadyEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import org.slf4j.Logger
import org.slf4j.simple.SimpleLoggerFactory
import java.util.Random
import java.util.Timer
import kotlin.concurrent.schedule

/**
 * Manages the status messages of the client
 */
object StatusOrchestrator : ListenerAdapter() {
    private val log: Logger = SimpleLoggerFactory().getLogger(this.javaClass.simpleName)
    private val playing = listOf(
        "Quasar", "Armax Arsenal Arena", "Alliance Corsair", "Towers of Hanoi", "Shattered Eezo",
        "Relay Defense", "Kepesh-Yakshi", "Firebreathing Thresher Maws of Doom", "Galaxy of Fantasy",
        "Third Coil", "Chess", "Nine Men's Morris", "with the space hamster")
    private val watching = listOf(
        "Armax Arsenal Arena", "ships take off", "ships dock", "Fleet and Flotilla", "Requiem for a Reaper!",
        "Blasto: the Jellyfish strings", "Blasto 6: Partners in Crime", "Blasto 7: Blasto Goes to War!",
        "Blasto 8: Blasto Cures the Genophage!", "Elcor Hamlet", "Starless", "Blue's Anatomy", "Call Me Sally",
        "Damaged: The Truth Behind the Citadel Crisis", "Exiles: Portraits of the Lost Quarians",
        "Last of the Legion", "Goda Tavetara: Lulea Reborn", "Saren: A Hero Betrayed", "The Demon and the Nightmare",
        "The Path of Lies: A History of the Alliance Military", "the extranet")
    private val listening = listOf(
        "skycars zoom by", "Expel 10", "Varrencage", "the hum of the ship", "the hum of a mass relay",
        "the silence of space", "diplomats")
    private val statuses: List<Game> = playing.map { Game.playing(it) }.plus(watching.map { Game.watching(it) }).plus(listening.map { Game.listening(it) })

    /**
     * When the client is ready
     */
    override fun onReady(event: ReadyEvent) {
        log.info("Ready on shard ${event.jda.shardInfo.shardId + 1}/${event.jda.shardInfo.shardTotal} with ${event.jda.guilds.count()} guilds")
        event.jda.presence.setPresence(OnlineStatus.ONLINE, getRandomStatus())
        Timer().schedule(1800000) {
            event.jda.presence.setPresence(OnlineStatus.ONLINE, getRandomStatus())
        }
    }

    /**
     * Change the presence of the client
     */
    fun setPresence(jda: JDA, status: OnlineStatus = jda.presence.status, game: Game = jda.presence.game) {
        jda.presence.setPresence(status, game)
    }

    private fun getRandomStatus(): Game {
        return statuses[Random().nextInt(statuses.size)]
    }
}
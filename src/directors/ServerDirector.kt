/*
 * ServerDirector.kt * Glyph, a Discord bot that uses natural language instead of commands
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

package me.ianmooreis.glyph.directors

import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.ianmooreis.glyph.directors.messaging.SimpleDescriptionBuilder
import me.ianmooreis.glyph.extensions.botRatio
import me.ianmooreis.glyph.extensions.isBotFarm
import me.ianmooreis.glyph.extensions.log
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.events.ReadyEvent
import net.dv8tion.jda.core.events.guild.GuildJoinEvent
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent
import org.json.JSONObject
import java.awt.Color
import java.net.URL
import java.time.Instant

/**
 * Manages server related events
 */
object ServerDirector : Director() {

    /**
     * When the client becomes ready
     */
    override fun onReady(event: ReadyEvent) {
        updateServerCount(event.jda)
        GlobalScope.launch {
            antiBotFarm(event.jda.guilds)
        }
    }

    /**
     * When the client joins a guild
     */
    override fun onGuildJoin(event: GuildJoinEvent) {
        updateServerCount(event.jda)
        event.jda.selfUser.log(getGuildEmbed(event.guild).setTitle("Guild Joined").setColor(Color.GREEN).build())
        log.info("Joined ${event.guild}")
    }

    /**
     * When the client leaves a guild
     */
    override fun onGuildLeave(event: GuildLeaveEvent) {
        updateServerCount(event.jda)
        event.jda.selfUser.log(getGuildEmbed(event.guild).setTitle("Guild Left").setColor(Color.RED).build())
        log.info("Left ${event.guild}")
    }

    /**
     * Automatically leave any guilds considered to be a bot farm
     *
     * @param guilds the list of guilds to check
     */
    private fun antiBotFarm(guilds: List<Guild>) {
        guilds.filter { it.isBotFarm }.forEach { guild ->
            guild.leave().queue {
                log.info("Left bot farm $guild! Guild had bot ratio of ${guild.botRatio}")
            }
        }
    }

    /**
     * Updates the server count on the bot list websites
     */
    private fun updateServerCount(jda: JDA) {
        val id = jda.selfUser.id
        val count = jda.guilds.count()
        val countJSON = JSONObject().put("server_count", count).put("shard_id", jda.shardInfo.shardId)
            .put("shard_count", jda.shardInfo.shardTotal)
        sendServerCount("https://discordbots.org/api/bots/$id/stats", countJSON, System.getenv("DISCORDBOTLIST_TOKEN"))
        sendServerCount("https://bots.discord.pw/api/bots/$id/stats", countJSON, System.getenv("DISCORDBOTS_TOKEN"))
    }

    /**
     * Sends a server count to a bot list website
     *
     * @param url the bot list website api url
     * @param countJSON the payload to send to the api
     * @param token the authentication token to use the api
     */
    private fun sendServerCount(url: String, countJSON: JSONObject, token: String) {
        url.httpPost().header("Authorization" to token, "Content-Type" to "application/json").body(countJSON.toString())
            .responseString { _, response, result ->
                val host = URL(url).host
                when (result) {
                    is Result.Success -> {
                        log.debug("Updated $host server count")
                    }
                    is Result.Failure -> {
                        log.warn("Failed to update $host server count due to ${response.statusCode} error!")
                    }
                }
            }
    }

    private fun getGuildEmbed(guild: Guild): EmbedBuilder {
        val description = SimpleDescriptionBuilder()
            .addField("Name", guild.name)
            .addField("ID", guild.id)
            .addField("Members", "${guild.members.size} (${guild.members.count { it.user.isBot }} bots)")
            .addField("Farm", "${guild.isBotFarm} (${"%.2f".format(guild.botRatio)})")
            .build()
        return EmbedBuilder()
            .setDescription(description)
            .setThumbnail(guild.iconUrl)
            .setFooter("Logging", null)
            .setTimestamp(Instant.now())
    }
}
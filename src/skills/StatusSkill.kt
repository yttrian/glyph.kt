/*
 * StatusSkill.kt
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

package me.ianmooreis.glyph.skills

import me.ianmooreis.glyph.Glyph
import me.ianmooreis.glyph.ai.AIResponse
import me.ianmooreis.glyph.database.Key
import me.ianmooreis.glyph.directors.messaging.SimpleDescriptionBuilder
import me.ianmooreis.glyph.directors.skills.Skill
import me.ianmooreis.glyph.extensions.isCreator
import me.ianmooreis.glyph.messaging.FormalResponse
import me.ianmooreis.glyph.messaging.Response
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDAInfo
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.ocpsoft.prettytime.PrettyTime
import redis.clients.jedis.JedisPool
import java.lang.management.ManagementFactory
import java.time.Instant
import java.util.Date

/**
 * A skill that shows users the current status of the client, with extra info for the creator only
 */
class StatusSkill(private val redisPool: JedisPool) : Skill("skill.status", cooldownTime = 5) {
    override suspend fun onTrigger(event: MessageReceivedEvent, ai: AIResponse): Response {
        val jda = event.jda
        val name = jda.selfUser.name
        val discordDescription = SimpleDescriptionBuilder()
            .addField("Ping", "${jda.gatewayPing} ms")
            .addField("Guilds", jda.guilds.count())
            .addField(
                "Shard",
                "${jda.shardInfo.shardId}${if (event.author.isCreator) "/${jda.shardInfo.shardTotal}" else ""}"
            )
            .addField("Users", jda.users.size)
        if (event.author.isCreator) {
            redisPool.resource.use {
                discordDescription.addField("Messages", it.get(Key.MESSAGE_COUNT.value) ?: "?")
            }
        }
        val embed = EmbedBuilder()
            .setTitle("$name Status")
            .addField("Discord", discordDescription.build(), true)
            .setFooter("$name-Kotlin-${Glyph.version}", null)
            .setTimestamp(Instant.now())
        if (event.author.isCreator) {
            val runtime = Runtime.getRuntime()
            val usedMemory = "%.2f".format((runtime.totalMemory() - runtime.freeMemory()).toFloat() / 1000000)
            val maxMemory = "%.2f".format(runtime.maxMemory().toFloat() / 1000000)
            val uptime = PrettyTime().format(Date(ManagementFactory.getRuntimeMXBean().startTime))
            val dynoDescription = SimpleDescriptionBuilder()
                .addField("Cores", runtime.availableProcessors())
                .addField("Memory", "$usedMemory of $maxMemory MB")
                .addField("JVM", Runtime.version().toString())
                .addField("Kotlin", KotlinVersion.CURRENT.toString())
                .addField("JDA", JDAInfo.VERSION)
                .addField("Restarted", uptime)
                .build()
            embed.addField("Dyno", dynoDescription, true)
        } else {
            embed.setThumbnail(jda.selfUser.avatarUrl)
        }
        embed.addField("Operating Parameters", ai.result.fulfillment.speech, true)

        return FormalResponse(embed = embed.build())
    }
}
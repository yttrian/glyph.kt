/*
 * StatusSkill.kt
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

package me.ianmooreis.glyph.bot.skills

import kotlinx.coroutines.future.await
import me.ianmooreis.glyph.bot.Glyph
import me.ianmooreis.glyph.bot.ai.AIResponse
import me.ianmooreis.glyph.bot.directors.config.Key
import me.ianmooreis.glyph.bot.directors.config.RedisAsync
import me.ianmooreis.glyph.bot.directors.messaging.SimpleDescriptionBuilder
import me.ianmooreis.glyph.bot.directors.skills.Skill
import me.ianmooreis.glyph.bot.extensions.isCreator
import me.ianmooreis.glyph.bot.messaging.Response
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDAInfo
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.ocpsoft.prettytime.PrettyTime
import java.lang.management.ManagementFactory
import java.time.Instant
import java.util.*

/**
 * A skill that shows users the current status of the client, with extra info for the creator only
 */
class StatusSkill(
    /**
     * Redis async connection
     */
    private val redis: RedisAsync
) : Skill("skill.status", cooldownTime = 5) {
    override suspend fun onTrigger(event: MessageReceivedEvent, ai: AIResponse): Response {
        val jda = event.jda
        val name = jda.selfUser.name
        val discordDescription = SimpleDescriptionBuilder()
            .addField("Ping", "${jda.gatewayPing} ms")
            .addField("Guilds", jda.guilds.size)
            .addField(
                "Shard",
                "${jda.shardInfo.shardId}" + if (event.author.isCreator) "/${jda.shardInfo.shardTotal}" else ""
            )
        if (event.author.isCreator) {
            val messageCount = redis.get(Key.MESSAGE_COUNT.value).await()
            discordDescription.addField("Messages", messageCount ?: "?")
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

        return Response.Volatile(embed.build())
    }
}

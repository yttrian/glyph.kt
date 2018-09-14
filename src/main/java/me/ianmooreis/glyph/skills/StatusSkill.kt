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

import ai.api.model.AIResponse
import me.ianmooreis.glyph.Glyph
import me.ianmooreis.glyph.extensions.isCreator
import me.ianmooreis.glyph.extensions.reply
import me.ianmooreis.glyph.orchestrators.messaging.MessagingOrchestrator
import me.ianmooreis.glyph.orchestrators.messaging.SimpleDescriptionBuilder
import me.ianmooreis.glyph.orchestrators.skills.Skill
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.JDAInfo
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import org.ocpsoft.prettytime.PrettyTime
import java.lang.management.ManagementFactory
import java.time.Instant
import java.util.Date

/**
 * A skill that shows users the current status of the client, with extra info for the creator only
 */
object StatusSkill : Skill("skill.status", cooldownTime = 5) {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        val jda = event.jda
        val name = jda.selfUser.name
        val discordDescription = SimpleDescriptionBuilder()
            .addField("Ping", "${jda.ping} ms")
            .addField("Guilds", jda.guilds.count())
            .addField("Shard", "${jda.shardInfo.shardId}${if (event.author.isCreator) "/${jda.shardInfo.shardTotal}" else ""}")
            .addField("Users", jda.users.size)
        if (event.author.isCreator) {
            discordDescription.addField("Messages", MessagingOrchestrator.getTotalMessages())
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
        embed.addField("Operating Parameters", ai.result.fulfillment.speech.replace("\\n", "\n", true), true)
        event.message.reply(embed = embed.build())
    }
}
/*
 * PurgeSkill.kt
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

package me.ianmooreis.glyph.bot.skills.moderation

import com.google.gson.JsonObject
import me.ianmooreis.glyph.bot.ai.AIResponse
import me.ianmooreis.glyph.bot.directors.AuditingDirector.audit
import me.ianmooreis.glyph.bot.directors.messaging.SimpleDescriptionBuilder
import me.ianmooreis.glyph.bot.directors.skills.Skill
import me.ianmooreis.glyph.bot.extensions.toDate
import me.ianmooreis.glyph.bot.messaging.Response
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.ocpsoft.prettytime.PrettyTime
import java.time.Duration
import java.time.OffsetDateTime
import java.util.concurrent.CompletableFuture

/**
 * A skill that allows privileged members to purge messages within a duration to the past
 */
class PurgeSkill : Skill("skill.moderation.purge", guildOnly = true) {
    override suspend fun onTrigger(event: MessageReceivedEvent, ai: AIResponse): Response {
        val time = event.message.timeCreated
        val durationEntity: JsonObject? = ai.result.getComplexParameter("duration")

        // Check that the user has permission within the channel
        if (!event.member!!.hasPermission(event.textChannel, Permission.MESSAGE_MANAGE)) {
            return Response.Volatile("You need permission to Manage Messages in this channel in order to purge messages!")
        }

        // Check that we have permission in the channel specifically, not the server as a whole
        if (!event.guild.selfMember.hasPermission(event.textChannel, Permission.MESSAGE_MANAGE)) {
            return Response.Volatile("I need permission to Manage Messages in this channel in order to purge messages!")
        } else if (!event.guild.selfMember.hasPermission(event.textChannel, Permission.MESSAGE_HISTORY)) {
            return Response.Volatile("I need permission to Read Message History in this channel in order to purge messages!")
        }

        // Warn the user if we can't determine what time duration they want
        if (durationEntity == null) {
            return Response.Volatile("That is an invalid time duration, try being less vague with abbreviations.")
        }

        val durationAmount = durationEntity.get("amount").asLong
        val duration = when (durationEntity.get("unit").asString) {
            "wk" -> time.minusWeeks(durationAmount)
            "day" -> time.minusDays(durationAmount)
            "h" -> time.minusHours(durationAmount)
            "min" -> time.minusMinutes(durationAmount)
            "s" -> time.minusSeconds(durationAmount)
            null -> time
            else -> null
        }
        if (duration === null || duration.isBefore(time.minusDays(14))) {
            return Response.Volatile("Discord only allows me to purge up to 14 days!")
        } else if (duration.isAfter(time)) {
            return Response.Volatile("I cannot purge messages from the future!")
        }

        val prettyDuration = PrettyTime().format(duration.toDate())
        event.textChannel.takeMessagesSince(duration).thenAcceptAsync {
            // JDA now handles chunking for bulk deletion, so we'll let it deal with that
            event.textChannel.purgeMessages(it)
        }

        // If purge auditing is enabled, log it
        if (event.guild.config.auditing.purge) {
            event.jda.addEventListener(object : ListenerAdapter() {
                val blame = event.author.asMention
                val channel = event.textChannel.asMention
                val reason = ai.result.getStringParameter("reason") ?: "No reason provided"

                override fun onMessageBulkDelete(event: MessageBulkDeleteEvent) {
                    val total = event.messageIds.size

                    val auditMessage = SimpleDescriptionBuilder()
                        .addField("Total", "$total messages")
                        .addField("Channel", channel)
                        .addField("Blame", blame)
                        .addField("Reason", reason)
                        .build()

                    event.guild.audit("Messages Purged", auditMessage)

                    event.jda.removeEventListener(this)
                }
            })
        }

        // Inform the user of a successful purge request
        return Response.Ephemeral(
            "Purging messages since $prettyDuration! (this may take a while depending on duration)",
            Duration.ofSeconds(10)
        )
    }

    private fun TextChannel.takeMessagesSince(time: OffsetDateTime): CompletableFuture<List<Message>> {
        return this.iterableHistory.takeWhileAsync { it.timeCreated.isAfter(time) }
    }
}

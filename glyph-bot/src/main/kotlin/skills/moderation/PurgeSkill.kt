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

package org.yttr.glyph.bot.skills.moderation

import com.google.gson.JsonObject
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.ocpsoft.prettytime.PrettyTime
import org.yttr.glyph.bot.ai.AIResponse
import org.yttr.glyph.bot.directors.AuditingDirector.audit
import org.yttr.glyph.bot.directors.messaging.SimpleDescriptionBuilder
import org.yttr.glyph.bot.directors.skills.Skill
import org.yttr.glyph.bot.extensions.toDate
import org.yttr.glyph.bot.messaging.Response
import java.time.Duration
import java.time.OffsetDateTime

/**
 * A skill that allows privileged members to purge messages within a duration to the past
 */
class PurgeSkill : Skill("skill.moderation.purge", guildOnly = true) {
    override suspend fun onTrigger(event: MessageReceivedEvent, ai: AIResponse): Response {
        val time = event.message.timeCreated
        val durationEntity: JsonObject? = ai.result.getComplexParameter("duration")

        // Check that the user has permission within the channel
        if (!event.member!!.hasPermission(event.textChannel, Permission.MESSAGE_MANAGE)) {
            return Response.Volatile(
                "You need permission to Manage Messages in this channel in order to purge messages!"
            )
        }

        // Check that we have permission in the channel specifically, not the server as a whole
        if (!event.guild.selfMember.hasPermission(event.textChannel, Permission.MESSAGE_MANAGE)) {
            return Response.Volatile(
                "I need permission to Manage Messages in this channel in order to purge messages!"
            )
        } else if (!event.guild.selfMember.hasPermission(event.textChannel, Permission.MESSAGE_HISTORY)) {
            return Response.Volatile(
                "I need permission to Read Message History in this channel in order to purge messages!"
            )
        }

        // Warn the user if we can't determine what time duration they want
        if (durationEntity == null) {
            return Response.Volatile(
                "That is an invalid time duration, try being less vague with abbreviations."
            )
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
        if (duration === null || duration.isBefore(time.minusDays(MAX_LOOKBACK_DAYS))) {
            return Response.Volatile("Discord only allows me to purge up to $MAX_LOOKBACK_DAYS days!")
        } else if (duration.isAfter(time)) {
            return Response.Volatile("I cannot purge messages from the future!")
        }

        val prettyDuration = PrettyTime().format(duration.toDate())

        delete(event, duration, ai.result.getStringParameter("reason") ?: "No reason provided")

        // Inform the user of a successful purge request
        return Response.Ephemeral(
            "Purging messages since $prettyDuration! (this may take a while depending on duration)",
            Duration.ofSeconds(10)
        )
    }

    private fun delete(event: MessageReceivedEvent, endTime: OffsetDateTime, reason: String) {
        fun deleteLoop(beforeMessage: Message, count: Int = 0) {
            event.textChannel.getHistoryBefore(beforeMessage, MESSAGE_RETRIEVAL_LIMIT).queue { messageHistory ->
                val messages = messageHistory.retrievedHistory
                if (messages.last().timeCreated.isAfter(endTime)) {
                    event.textChannel.purgeMessages(messages)
                    deleteLoop(messages.last(), count + messages.size)
                } else {
                    val remainingMessages = messages.takeWhile { it.timeCreated.isAfter(endTime) }
                    event.textChannel.purgeMessages(remainingMessages)

                    // If purge auditing is enabled, log it
                    if (event.guild.config.auditing.purge) {
                        val blame = event.author.asMention
                        val channel = event.textChannel.asMention

                        val auditMessage = SimpleDescriptionBuilder()
                            .addField("Total", "${count + remainingMessages.size} messages")
                            .addField("Channel", channel)
                            .addField("Blame", blame)
                            .addField("Reason", reason)
                            .build()

                        event.guild.audit("Messages Purged", auditMessage)
                    }
                }
            }
        }

        event.message.apply {
            delete().queue()
            deleteLoop(this)
        }
    }

    companion object {
        private const val MESSAGE_RETRIEVAL_LIMIT: Int = 100
        private const val MAX_LOOKBACK_DAYS: Long = 14
    }
}

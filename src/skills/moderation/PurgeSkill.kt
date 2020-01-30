/*
 * PurgeSkill.kt
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

package me.ianmooreis.glyph.skills.moderation

import com.google.gson.JsonObject
import me.ianmooreis.glyph.directors.messaging.AIResponse
import me.ianmooreis.glyph.directors.messaging.CustomEmote
import me.ianmooreis.glyph.directors.messaging.SimpleDescriptionBuilder
import me.ianmooreis.glyph.directors.skills.Skill
import me.ianmooreis.glyph.extensions.*
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.ocpsoft.prettytime.PrettyTime

/**
 * A skill that allows privileged members to purge messages within a duration to the past
 */
object PurgeSkill : Skill("skill.moderation.purge", guildOnly = true) {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        val time = event.message.creationTime
        val durationEntity: JsonObject? = ai.result.getComplexParameter("duration")

        // Check that the user has permission within the channel
        if (!event.member.hasPermission(event.textChannel, Permission.MESSAGE_MANAGE)) {
            event.message.reply("You need permission to Manage Messages in this channel in order to purge messages!")
            return
        }

        // Check that we have permission in the channel specifically, not the server as a whole
        if (!event.guild.selfMember.hasPermission(event.textChannel, Permission.MESSAGE_MANAGE)) {
            event.message.reply("I need permission to Manage Messages in this channel in order to purge messages!")
            return
        } else if (!event.guild.selfMember.hasPermission(event.textChannel, Permission.MESSAGE_HISTORY)) {
            event.message.reply("I need permission to Read Message History in this channel in order to purge messages!")
            return
        }

        // Warn the user if we can't determine what time duration they want
        if (durationEntity == null) {
            event.message.reply("${CustomEmote.XMARK} That is an invalid time duration, try being less vague with abbreviations.")
            return
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
            event.message.reply("${CustomEmote.XMARK} Discord only allows me to purge up to 14 days!")
            return
        } else if (duration.isAfter(time)) {
            event.message.reply("${CustomEmote.XMARK} I cannot purge messages from the future!")
            return
        }

        val prettyDuration = PrettyTime().format(duration.toDate())
        val messages = event.textChannel.getMessagesSince(duration)
        if (messages.size > 2) {
            // JDA now handles chunking for bulk deletion, so we'll let it deal with that
            event.textChannel.purgeMessages(messages)
            // Inform the use of a successful purge request
            event.message.reply(
                "${CustomEmote.CHECKMARK} ${messages.size} messages since $prettyDuration " +
                    if (messages.size > 100) "queued for purging!" else "purged!"
                , deleteAfterDelay = 10
            )
            // If purge auditing is enabled, log it
            if (event.guild.config.auditing.purge) {
                val reason = ai.result.getStringParameter("reason") ?: "No reason provided"
                val auditMessage = SimpleDescriptionBuilder()
                    .addField("Total", "${messages.size} messages")
                    .addField("Channel", event.textChannel.asMention)
                    .addField("Blame", event.author.asMention)
                    .addField("Reason", reason)
                    .build()
                event.guild.audit("Messages Purged", auditMessage)
            }
        } else {
            event.message.delete().reason("Failed purge request").queue()
            event.message.reply(
                "${CustomEmote.XMARK} There must be at least two messages to purge! Try a longer duration.",
                deleteAfterDelay = 10
            )
        }
    }
}

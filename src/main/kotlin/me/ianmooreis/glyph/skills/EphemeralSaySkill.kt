/*
 * EphemeralSaySkill.kt
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

import com.google.gson.JsonObject
import me.ianmooreis.glyph.directors.messaging.AIResponse
import me.ianmooreis.glyph.directors.skills.Skill
import me.ianmooreis.glyph.extensions.reply
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.time.Instant
import java.util.concurrent.TimeUnit

/**
 * Allows users to briefly say something before it is deleted automatically
 */
object EphemeralSaySkill :
    Skill("skill.ephemeral_say", requiredPermissionsSelf = listOf(Permission.MESSAGE_MANAGE), guildOnly = true) {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        val durationEntity: JsonObject? = ai.result.getComplexParameter("duration")
        if (durationEntity == null) {
            event.message.reply(
                "That is an invalid time duration, specify how many seconds you want your message to last.",
                deleteAfterDelay = 5,
                deleteAfterUnit = TimeUnit.SECONDS
            )
            return
        }

        val durationAmount = durationEntity.get("amount").asLong
        val durationUnit = when (durationEntity.get("unit").asString) {
            "s" -> TimeUnit.SECONDS
            null -> null
            else -> null
        }
        if (durationUnit == null || durationAmount > 30) {
            event.message.reply(
                "You can only say something ephemerally for less than 30 seconds!",
                deleteAfterDelay = 5,
                deleteAfterUnit = TimeUnit.SECONDS
            )
            return
        } else if (durationAmount <= 0) {
            event.message.reply(
                "You can only say something ephemerally for a positive amount of time!",
                deleteAfterDelay = 5,
                deleteAfterUnit = TimeUnit.SECONDS
            )
            return
        }

        event.message.delete().reason("Ephemeral Say").queue()
        event.message.reply(
            EmbedBuilder()
                .setAuthor(event.author.name, null, event.author.avatarUrl)
                .setDescription(ai.result.getStringParameter("message"))
                .setFooter("Ephemeral Say", null)
                .setTimestamp(Instant.now().plus(durationAmount, durationUnit.toChronoUnit()))
                .build(),
            deleteAfterDelay = durationAmount, deleteAfterUnit = durationUnit
        )
    }
}
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
import me.ianmooreis.glyph.ai.AIResponse
import me.ianmooreis.glyph.directors.skills.Skill
import me.ianmooreis.glyph.messaging.response.EphemeralResponse
import me.ianmooreis.glyph.messaging.response.Response
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Allows users to briefly say something before it is deleted automatically
 */
class EphemeralSaySkill :
    Skill("skill.ephemeral_say", requiredPermissionsSelf = listOf(Permission.MESSAGE_MANAGE), guildOnly = true) {
    override suspend fun onTrigger(event: MessageReceivedEvent, ai: AIResponse): Response {
        val durationEntity: JsonObject = ai.result.getComplexParameter("duration")
            ?: return EphemeralResponse(
                "That is an invalid time duration, specify how many seconds you want your message to last.",
                ttl = Duration.ofSeconds(5)
            )

        val durationAmount = durationEntity.get("amount").asLong
        val durationUnit = when (durationEntity.get("unit").asString) {
            "s" -> ChronoUnit.SECONDS
            else -> null
        }
        if (durationUnit == null || durationAmount > 30) {
            return EphemeralResponse(
                "You can only say something ephemerally for less than 30 seconds!",
                ttl = Duration.ofSeconds(5)
            )
        } else if (durationAmount <= 0) {
            return EphemeralResponse(
                "You can only say something ephemerally for a positive amount of time!",
                ttl = Duration.ofSeconds(5)
            )
        }

        event.message.delete().reason("Ephemeral Say").queue()
        return EphemeralResponse(
            embed = EmbedBuilder()
                .setAuthor(event.author.name, null, event.author.avatarUrl)
                .setDescription(ai.result.getStringParameter("message"))
                .setFooter("Ephemeral Say", null)
                .setTimestamp(Instant.now().plus(durationAmount, durationUnit))
                .build(),
            ttl = Duration.of(durationAmount, durationUnit)
        )
    }
}
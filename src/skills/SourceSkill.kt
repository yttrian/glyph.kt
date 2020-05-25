/*
 * SourceSkill.kt
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
import me.ianmooreis.glyph.directors.skills.Skill
import me.ianmooreis.glyph.messaging.Response
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.awt.Color
import java.time.Instant

/**
 * A skill that allows users to see the license and link to the source code
 */
class SourceSkill : Skill("skill.source") {
    override suspend fun onTrigger(event: MessageReceivedEvent, ai: AIResponse): Response {
        val name = event.jda.selfUser.name
        val embed = EmbedBuilder()
            .setTitle("$name Source")
            .setDescription(ai.result.fulfillment.speech)
            .setFooter("$name-Kotlin-${Glyph.version}", null)
            .setTimestamp(Instant.now())
            .setColor(Color.getHSBColor(0.6f, 0.89f, 0.61f))
            .build()

        return Response.Volatile(embed)
    }
}
/*
 * SnowstampSkill.kt
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

import me.ianmooreis.glyph.ai.AIResponse
import me.ianmooreis.glyph.directors.messaging.SimpleDescriptionBuilder
import me.ianmooreis.glyph.directors.skills.Skill
import me.ianmooreis.glyph.messaging.response.VolatileResponse
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.utils.TimeUtil
import java.awt.Color

/**
 * A skill that allows users to get a timestamp from a Discord snowflake id
 */
class SnowstampSkill : Skill("skill.snowstamp") {
    override suspend fun onTrigger(event: MessageReceivedEvent, ai: AIResponse): VolatileResponse {
        val snowflake = ai.result.getStringParameter("snowflake") ?: ""
        val snowflakeId = try {
            snowflake.toLong()
        } catch (e: NumberFormatException) {
            return VolatileResponse("`$snowflake` is not a snowflake!")
        }
        val snowflakeInstant = TimeUtil.getTimeCreated(snowflakeId).toInstant()
        val description = SimpleDescriptionBuilder()
            .addField("UTC", snowflakeInstant.toString())
            .addField("UNIX", snowflakeInstant.toEpochMilli())
            .build()
        return VolatileResponse(
            embed = EmbedBuilder()
                .setTitle(snowflakeId.toString())
                .setDescription(description)
                .setColor(Color.WHITE)
                .setFooter("Snowstamp", null)
                .setTimestamp(snowflakeInstant)
                .build()
        )
    }
}
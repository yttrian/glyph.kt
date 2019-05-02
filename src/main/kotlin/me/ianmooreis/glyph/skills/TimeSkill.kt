/*
 * TimeSkill.kt
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
import me.ianmooreis.glyph.directors.skills.Skill
import me.ianmooreis.glyph.extensions.reply
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

/**
 * A skill that attempts to show the time in other timezones
 */
object TimeSkill : Skill("skill.time") {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        val df = SimpleDateFormat("**HH:mm:ss** 'on' EEEE, MMMM dd, yyyy")
        df.timeZone = TimeZone.getTimeZone(ai.result.getStringParameter("timezone"))
        event.message.reply(
            EmbedBuilder()
                .setTitle(df.timeZone.displayName)
                .setDescription(df.format(Date()))
                .setFooter("Time", null)
                .setTimestamp(Instant.now())
                .build()
        )
    }
}
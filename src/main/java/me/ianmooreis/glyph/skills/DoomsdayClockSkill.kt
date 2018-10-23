/*
 * DoomsdayClockSkill.kt
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
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import me.ianmooreis.glyph.extensions.reply
import me.ianmooreis.glyph.orchestrators.skills.Skill
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.time.Instant

/**
 * A skill that allows users to see the current Doomsday Clock status
 */
object DoomsdayClockSkill : Skill("skill.doomsday_clock") {
    private val timeRegex = Regex("(IT IS (.*?) TO MIDNIGHT)", RegexOption.IGNORE_CASE)
    private val reasonRegex = Regex("<div class=\"uabb-infobox-text uabb-text-editor\"><p>(.*)(?:See the|Read the)", RegexOption.IGNORE_CASE)

    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        "https://thebulletin.org/timeline".httpGet().responseString { _, _, result ->
            when (result) {
                is Result.Success -> {
                    val content = result.get()
                    val minutesToMidnight = timeRegex.findAll(content).first().groups[1]?.value ?: "Unknown"
                    val reason = reasonRegex.find(content)?.groups?.get(2)?.value
                    event.message.reply(EmbedBuilder()
                        .setTitle(minutesToMidnight, "https://thebulletin.org/timeline")
                        .setDescription(reason)
                        .setFooter("Doomsday Clock", null)
                        .setTimestamp(Instant.now())
                        .build())
                }
                is Result.Failure -> {
                    event.message.reply("I was unable to check the Doomsday Clock!")
                }
            }
        }
    }
}
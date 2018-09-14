/*
 * HelpSkill.kt
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
import me.ianmooreis.glyph.extensions.reply
import me.ianmooreis.glyph.orchestrators.skills.Skill
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.awt.Color

/**
 * A skill that shows users a help messgae
 */
object HelpSkill : Skill("skill.help") {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        //val creator: User = event.jda.getUserById(System.getenv("CREATOR_ID"))
        val name = event.jda.selfUser.name
        val embed = EmbedBuilder()
            .setTitle("$name Help")
            .setDescription(ai.result.fulfillment.speech.replace("\\n", "\n", true))
            .setColor(Color.getHSBColor(0.6f, 0.89f, 0.61f))
            .build()
        event.message.reply(embed = embed)
    }
}
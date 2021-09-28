/*
 * HelpSkill.kt
 *
 * Glyph, a Discord bot that uses natural language instead of commands
 * powered by DialogFlow and Kotlin
 *
 * Copyright (C) 2017-2021 by Ian Moore
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

package org.yttr.glyph.bot.skills.util

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Emoji
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.Button
import net.dv8tion.jda.api.interactions.components.ButtonStyle
import org.yttr.glyph.bot.ai.AIResponse
import org.yttr.glyph.bot.messaging.Response
import org.yttr.glyph.bot.skills.Skill
import java.awt.Color

/**
 * A skill that shows users a help message
 */
class HelpSkill : Skill("skill.help") {
    override suspend fun onTrigger(event: MessageReceivedEvent, ai: AIResponse): Response {
        val name = event.jda.selfUser.name
        val embed = EmbedBuilder()
            .setTitle("$name Help")
            .setDescription(ai.result.fulfillment.speech)
            .setColor(embedColor)
            .build()

        return Response.Volatile(embed, actionRow)
    }

    companion object {
        private val embedColor = Color.decode("#4687E5")

        private fun linkButton(url: String, label: String, emoji: String) =
            Button.of(ButtonStyle.LINK, url, label, Emoji.fromUnicode(emoji))

        private val skillsButton = linkButton("https://gl.yttr.org/en/latest/skills.html", "Skills", "🕺")
        private val serverButton = linkButton("https://gl.yttr.org/server", "Help server", "✋")
        private val inviteButton = linkButton("https://gl.yttr.org/invite", "Invite", "📩")
        private val supportButton = linkButton("https://gl.yttr.org/sponsor", "Sponsor", "💖")
        private val actionRow = ActionRow.of(skillsButton, serverButton, inviteButton, supportButton)
    }
}

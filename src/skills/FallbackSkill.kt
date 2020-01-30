/*
 * FallbackSkill.kt
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

import me.ianmooreis.glyph.directors.messaging.AIResponse
import me.ianmooreis.glyph.directors.skills.Skill
import me.ianmooreis.glyph.extensions.reply
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException

/**
 * A skill that handles when a skill can't be found and there's no response from DialogFlow
 */
object FallbackSkill : Skill("fallback.primary", cooldownTime = 0) {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        try {
            event.message.addReaction("❓").queue()
        } catch (e: InsufficientPermissionException) {
            event.message.reply(ai.result.fulfillment.speech)
        }
    }
}
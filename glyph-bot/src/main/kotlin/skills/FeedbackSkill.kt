/*
 * FeedbackSkill.kt
 *
 * Glyph, a Discord bot that uses natural language instead of commands
 * powered by DialogFlow and Kotlin
 *
 * Copyright (C) 2017-2020 by Ian Moore
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

package org.yttr.glyph.bot.skills

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.yttr.glyph.bot.ai.AIResponse
import org.yttr.glyph.bot.directors.skills.Skill
import org.yttr.glyph.bot.extensions.log
import org.yttr.glyph.bot.messaging.Response

/**
 * A skill that allows users to send anonymous feedback via the global log webhook
 */
class FeedbackSkill : Skill("skill.feedback", cooldownTime = 90) {
    override suspend fun onTrigger(event: MessageReceivedEvent, ai: AIResponse): Response {
        event.jda.selfUser.log("Feedback", "```${ai.result.getStringParameter("feedback")}```")
        return Response.Volatile(ai.result.fulfillment.speech)
    }
}

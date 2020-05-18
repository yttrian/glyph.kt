/*
 * ChangeStatusSkill.kt
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

package me.ianmooreis.glyph.skills.creator

import me.ianmooreis.glyph.ai.AIResponse
import me.ianmooreis.glyph.directors.StatusDirector
import me.ianmooreis.glyph.directors.skills.Skill
import me.ianmooreis.glyph.messaging.response.Response
import me.ianmooreis.glyph.messaging.response.VolatileResponse
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

/**
 * A skill that allows the creator to change the client status
 */
class ChangeStatusSkill : Skill("skill.creator.changeStatus", creatorOnly = true, cooldownTime = 30) {
    override suspend fun onTrigger(event: MessageReceivedEvent, ai: AIResponse): Response {
        val jda = event.jda
        val name = ai.result.getStringParameter("status") ?: jda.presence.activity?.name ?: "?"
        val streamUrl = ai.result.getStringParameter("streamUrl") ?: jda.presence.activity?.url ?: "?"
        val gameType = ai.result.getStringParameter("gameType")
        val statusType = ai.result.getStringParameter("statusType")

        val game = when (gameType) {
            "playing" -> Activity.playing(name)
            "listening" -> Activity.listening(name)
            "watching" -> Activity.watching(name)
            "streaming" -> Activity.streaming(name, streamUrl)
            else -> jda.presence.activity
        }
        val status = when (statusType) {
            "online" -> OnlineStatus.ONLINE
            "idle" -> OnlineStatus.IDLE
            "dnd" -> OnlineStatus.DO_NOT_DISTURB
            "invisible" -> OnlineStatus.INVISIBLE
            else -> jda.presence.status
        }

        StatusDirector.setPresence(jda, status, game)

        return VolatileResponse(
            "Attempted to changed presence to ${status.name.toLowerCase()} " +
                "while ${game?.type.toString().toLowerCase()} to ${game?.name}! (May be rate limited)"
        )
    }
}
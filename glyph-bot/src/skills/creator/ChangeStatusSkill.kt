package org.yttr.glyph.bot.skills.creator

import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.yttr.glyph.bot.ai.AIResponse
import org.yttr.glyph.bot.messaging.Response
import org.yttr.glyph.bot.presentation.StatusDirector
import org.yttr.glyph.bot.skills.Skill

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

        return Response.Volatile(
            "Attempted to changed presence to ${status.name.toLowerCase()} " +
                    "while ${game?.type.toString().toLowerCase()} to ${game?.name}! (May be rate limited)"
        )
    }
}

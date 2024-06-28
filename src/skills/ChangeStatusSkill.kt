package org.yttr.glyph.skills

import dev.kord.common.entity.PresenceStatus
import dev.kord.core.event.message.MessageCreateEvent
import org.yttr.glyph.ai.AIResponse

/**
 * A skill that allows the creator to change the client status
 */
class ChangeStatusSkill : Skill("skill.creator.changeStatus", creatorOnly = true) {
    override suspend fun perform(event: MessageCreateEvent, ai: AIResponse) {
        val name = ai.result.getStringParameter("status") ?: jda.presence.activity?.name ?: "?"
        val streamUrl = ai.result.getStringParameter("streamUrl") ?: jda.presence.activity?.url ?: "?"
        val gameType = ai.result.getStringParameter("gameType")
        val statusType = ai.result.getStringParameter("statusType")

        event.kord.editPresence {
            when (statusType) {
                "online" -> status = PresenceStatus.Online
                "idle" -> status = PresenceStatus.Idle
                "dnd" -> status = PresenceStatus.DoNotDisturb
                "invisible" -> status = PresenceStatus.Invisible
            }

            when (gameType) {
                "playing" -> playing(name)
                "listening" -> listening(name)
                "watching" -> watching(name)
                "streaming" -> streaming(name, streamUrl)
            }
        }

        event.reply {
            content = "Attempted to change presence to $statusType while $gameType to $name!"
        }
    }
}

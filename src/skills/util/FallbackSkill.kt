package org.yttr.glyph.bot.skills.util

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import org.yttr.glyph.bot.ai.AIResponse
import org.yttr.glyph.bot.messaging.Response
import org.yttr.glyph.bot.skills.Skill

/**
 * A skill that handles when a skill can't be found and there's no response from DialogFlow
 */
class FallbackSkill : Skill("fallback.primary", cooldownTime = 0) {
    override suspend fun onTrigger(event: MessageReceivedEvent, ai: AIResponse): Response {
        try {
            event.message.addReaction("❓").queue()
        } catch (e: InsufficientPermissionException) {
            return Response.Volatile(ai.result.fulfillment.speech)
        }

        return Response.None
    }
}

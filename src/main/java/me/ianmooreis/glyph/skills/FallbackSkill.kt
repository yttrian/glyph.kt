package me.ianmooreis.glyph.skills

import ai.api.model.AIResponse
import me.ianmooreis.glyph.extensions.reply
import me.ianmooreis.glyph.orchestrators.skills.Skill
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
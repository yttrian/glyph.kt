package org.yttr.glyph.skills

import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.request.RestRequestException
import org.slf4j.LoggerFactory
import org.yttr.glyph.ai.AIResponse

/**
 * A skill that handles when a skill can't be found and there's no response from DialogFlow
 */
object FallbackSkill : Skill("fallback.primary") {
    private val log = LoggerFactory.getLogger(this::class.java)

    override suspend fun perform(event: MessageCreateEvent, ai: AIResponse) {
        try {
            event.message.addReaction(ReactionEmoji.Unicode("❓"))
        } catch (e: RestRequestException) {
            log.warn(e.message)
            event.reply { content = ai.result.fulfillment.speech }
        }
    }
}

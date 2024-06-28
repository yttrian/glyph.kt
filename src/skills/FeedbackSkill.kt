package org.yttr.glyph.skills

import dev.kord.core.event.message.MessageCreateEvent
import org.yttr.glyph.ai.AIResponse

/**
 * A skill that allows users to send anonymous feedback via the global log webhook
 */
class FeedbackSkill : Skill("skill.feedback") {
    override suspend fun perform(event: MessageCreateEvent, ai: AIResponse) {
        // TODO: event.jda.selfUser.log("Feedback", "```${ai.result.getStringParameter("feedback")}```")

        event.reply {
            ai.result.fulfillment.speech
        }
    }
}

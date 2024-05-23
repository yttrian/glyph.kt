package org.yttr.glyph.skills.util

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.yttr.glyph.ai.AIResponse
import org.yttr.glyph.extensions.log
import org.yttr.glyph.messaging.Response
import org.yttr.glyph.skills.Skill

/**
 * A skill that allows users to send anonymous feedback via the global log webhook
 */
class FeedbackSkill : Skill("skill.feedback", cooldownTime = 90) {
    override suspend fun onTrigger(event: MessageReceivedEvent, ai: AIResponse): Response {
        event.jda.selfUser.log("Feedback", "```${ai.result.getStringParameter("feedback")}```")
        return Response.Volatile(ai.result.fulfillment.speech)
    }
}

package me.ianmooreis.glyph.skills

import ai.api.model.AIResponse
import me.ianmooreis.glyph.extensions.log
import me.ianmooreis.glyph.extensions.reply
import me.ianmooreis.glyph.orchestrators.skills.Skill
import net.dv8tion.jda.core.events.message.MessageReceivedEvent

/**
 * A skill that allows users to send anonymous feedback via the global log webhook
 */
object FeedbackSkill : Skill("skill.feedback", cooldownTime = 90) {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        event.jda.selfUser.log("Feedback", "```${ai.result.getStringParameter("feedback")}```")
        event.message.reply(ai.result.fulfillment.speech)
    }
}
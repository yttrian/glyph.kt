package me.ianmooreis.glyph.skills

import ai.api.model.AIResponse
import me.ianmooreis.glyph.extensions.log
import me.ianmooreis.glyph.extensions.reply
import me.ianmooreis.glyph.orchestrators.SkillAdapter
import net.dv8tion.jda.core.events.message.MessageReceivedEvent

object FeedbackSkill : SkillAdapter("skill.feedback") {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        event.jda.selfUser.log("Feedback", "```${ai.result.getStringParameter("feedback")}```")
        event.message.reply(ai.result.fulfillment.speech)
    }
}
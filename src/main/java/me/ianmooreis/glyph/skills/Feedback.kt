package me.ianmooreis.glyph.skills

import ai.api.model.AIResponse
import me.ianmooreis.glyph.extensions.reply
import me.ianmooreis.glyph.orchestrators.Skill
import me.ianmooreis.glyph.utils.webhooks.LoggingWebhook
import net.dv8tion.jda.core.events.message.MessageReceivedEvent

object FeedbackSkill : Skill("skill.feedback") {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        LoggingWebhook.log(
                "Feedback",
                "${event.author} says: ```${ai.result.getStringParameter("feedback")}```",
                event.jda.selfUser)
        event.message.reply(ai.result.fulfillment.speech)
    }
}
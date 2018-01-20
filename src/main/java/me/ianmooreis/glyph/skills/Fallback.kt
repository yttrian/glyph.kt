package me.ianmooreis.glyph.skills

import ai.api.model.AIResponse
import me.ianmooreis.glyph.orchestrators.Skill
import net.dv8tion.jda.core.events.message.MessageReceivedEvent

object FallbackSkill: Skill("skill.fallback") {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        event.message.addReaction("❓").queue()
    }
}
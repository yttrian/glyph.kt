package me.ianmooreis.glyph.skills

import ai.api.model.AIResponse
import me.ianmooreis.glyph.orchestrators.skills.SkillAdapter
import net.dv8tion.jda.core.events.message.MessageReceivedEvent

object FallbackSkill : SkillAdapter("fallback.primary", cooldownTime = 0) {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        event.message.addReaction("❓").queue()
    }
}
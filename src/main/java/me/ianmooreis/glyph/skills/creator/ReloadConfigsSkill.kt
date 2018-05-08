package me.ianmooreis.glyph.skills.creator

import ai.api.model.AIResponse
import me.ianmooreis.glyph.extensions.reply
import me.ianmooreis.glyph.orchestrators.DatabaseOrchestrator
import me.ianmooreis.glyph.orchestrators.skills.SkillAdapter
import net.dv8tion.jda.core.events.message.MessageReceivedEvent

object ReloadConfigsSkill : SkillAdapter("skill.creator.reloadConfigs", creatorOnly = true) {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        DatabaseOrchestrator.loadConfigs()
        event.message.reply("Reloaded all configs.")
    }
}
package me.ianmooreis.glyph.skills.creator

import ai.api.model.AIResponse
import me.ianmooreis.glyph.extensions.reply
import me.ianmooreis.glyph.orchestrators.DatabaseOrchestrator
import me.ianmooreis.glyph.orchestrators.skills.Skill
import net.dv8tion.jda.core.events.message.MessageReceivedEvent

/**
 * A skill that allows the creator to ask the client to reload all configs from the database
 */
object ReloadConfigsSkill : Skill("skill.creator.reloadConfigs", creatorOnly = true) {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        DatabaseOrchestrator.loadConfigs()
        event.message.reply("Reloaded all configs.")
    }
}
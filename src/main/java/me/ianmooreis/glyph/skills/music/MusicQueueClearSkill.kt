package me.ianmooreis.glyph.skills.music

import ai.api.model.AIResponse
import me.ianmooreis.glyph.extensions.reply
import me.ianmooreis.glyph.orchestrators.SkillAdapter
import net.dv8tion.jda.core.events.message.MessageReceivedEvent

object MusicQueueClearSkill : SkillAdapter("skill.music.queue.clear", serverOnly = true) {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        val musicManager = MusicSkillManager.getOrCreateManager(event.guild)
        musicManager.clearQueue()
        event.message.reply("Queue cleared!")
    }
}
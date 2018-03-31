package me.ianmooreis.glyph.skills.music

import ai.api.model.AIResponse
import me.ianmooreis.glyph.extensions.reply
import me.ianmooreis.glyph.orchestrators.Skill
import net.dv8tion.jda.core.events.message.MessageReceivedEvent

object MusicSkipSkill : Skill("skill.music.skip", serverOnly = true) {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        val voiceChannel = event.guild.audioManager.connectedChannel
        if (event.member.voiceState.channel == voiceChannel) {
            MusicSkillManager.getOrCreateManager(event.guild).skip()
            event.message.reply("Skipping to next song!")
        }
    }
}
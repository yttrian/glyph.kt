package me.ianmooreis.glyph.skills.music

import ai.api.model.AIResponse
import me.ianmooreis.glyph.extensions.reply
import me.ianmooreis.glyph.orchestrators.SkillAdapter
import net.dv8tion.jda.core.events.message.MessageReceivedEvent

object MusicStopSkill : SkillAdapter("skill.music.stop", serverOnly = true) {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        val voiceChannel = event.guild.audioManager.connectedChannel
        if (event.member.voiceState.channel == voiceChannel) {
            MusicSkillManager.getOrCreateManager(event.guild).stop()
            event.guild.audioManager.closeAudioConnection()
            event.message.reply("Stopped music!")
        }
    }
}
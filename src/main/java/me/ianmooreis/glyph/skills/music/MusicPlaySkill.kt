package me.ianmooreis.glyph.skills.music

import ai.api.model.AIResponse
import me.ianmooreis.glyph.extensions.reply
import me.ianmooreis.glyph.orchestrators.Skill
import net.dv8tion.jda.core.events.message.MessageReceivedEvent


object MusicPlaySkill : Skill("skill.music.play", serverOnly = true) {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        val voiceChannel = event.member.voiceState.channel
        val query = ai.result.getStringParameter("query", null) ?: ai.result.getStringParameter("url")?.replace("\\", "")
        when {
            voiceChannel == null -> event.message.reply("You must be in a voice channel!")
            query == null -> event.message.reply("You must be input a query!")
            else -> {
                val audioManager = event.guild.audioManager
                val musicManager = MusicSkillManager.getOrCreateManager(event.guild)
                audioManager.sendingHandler = musicManager.sendHandler
                if (!audioManager.isConnected) audioManager.openAudioConnection(voiceChannel)
                musicManager.queue(event.message, query)
            }
        }
    }
}
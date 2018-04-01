package me.ianmooreis.glyph.skills.music

import ai.api.model.AIResponse
import me.ianmooreis.glyph.extensions.reply
import me.ianmooreis.glyph.orchestrators.SkillAdapter
import net.dv8tion.jda.core.events.message.MessageReceivedEvent


object MusicPlaySkill : SkillAdapter("skill.music.play", serverOnly = true) {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        val voiceChannel = event.member.voiceState.channel
        val query = ai.result.getStringParameter("query", null)
        val url = ai.result.getStringParameter("url")?.replace("\\", "")
        val identifier =  if (query != null) "ytsearch:$query" else url
        when {
            voiceChannel == null -> event.message.reply("You must be in a voice channel!")
            identifier == null -> event.message.reply("You must be input a query!")
            else -> {
                val audioManager = event.guild.audioManager
                val musicManager = MusicSkillManager.getOrCreateManager(event.guild)
                audioManager.sendingHandler = musicManager.sendHandler
                if (!audioManager.isConnected) {
                    audioManager.openAudioConnection(voiceChannel)
                    audioManager.connectTimeout = 5000
                }
                musicManager.queue(event.message, identifier)
            }
        }
    }
}
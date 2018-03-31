package me.ianmooreis.glyph.skills.music

import ai.api.model.AIResponse
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import me.ianmooreis.glyph.extensions.reply
import me.ianmooreis.glyph.orchestrators.Skill
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.time.Duration
import java.time.Instant

object MusicQueueSkill : Skill("skill.music.queue", serverOnly = true) {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        val voiceChannel = event.guild.audioManager.connectedChannel
        if (event.member.voiceState.channel == voiceChannel) {
            val musicManager = MusicSkillManager.getOrCreateManager(event.guild)
            val queue = musicManager.getQueue()
            val nowPlaying = musicManager.nowPlaying
            val queueString = queue.joinToString("\n") { trackInfo(it) }
            val embed = EmbedBuilder().setTitle("Queue").setFooter("Music", null).setTimestamp(Instant.now())
            if (nowPlaying != null) {
                embed.addField("Now playing", trackInfo(nowPlaying), false)
                        .addField("Up next", if (queueString.isNotEmpty()) queueString else "The queue is empty. Try adding some songs!", false)
            } else {
                embed.setDescription("The queue is empty. Try adding some songs!")
            }
            event.message.reply(embed.build())
        }
    }

    private fun trackInfo(track: AudioTrack): String {
        return "**[${track.info.title}](${track.info.uri})**\n${track.info.author} - ${Duration.ofMillis(track.duration).toString().removePrefix("PT").toLowerCase()}"
    }
}
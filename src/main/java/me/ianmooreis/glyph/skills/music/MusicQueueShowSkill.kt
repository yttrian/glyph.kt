package me.ianmooreis.glyph.skills.music

import ai.api.model.AIResponse
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import me.ianmooreis.glyph.extensions.reply
import me.ianmooreis.glyph.orchestrators.SkillAdapter
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.time.Duration
import java.time.Instant

object MusicQueueShowSkill : SkillAdapter("skill.music.queue.show", serverOnly = true) {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        val musicManager = MusicSkillManager.getOrCreateManager(event.guild)
        val queue = musicManager.getQueue()
        val nowPlaying = musicManager.nowPlaying
        val queueString = queue.subList(0, if (queue.size <= 5) queue.size else 5).joinToString("\n") { trackInfo(it, queue.indexOf(it)) }
        val embed = EmbedBuilder().setTitle("Queue").setFooter("Music", null).setTimestamp(Instant.now())
        if (nowPlaying != null) {
            embed.addField("Now playing", trackInfo(nowPlaying), false)
                    .addField("Up next (${queue.size} songs)", if (queueString.isNotEmpty()) queueString else "The queue is empty. Try adding some songs!", false)
        } else {
            embed.setDescription("The queue is empty. Try adding some songs!")
        }
        event.message.reply(embed.build())
    }

    private fun trackInfo(track: AudioTrack, number: Int? = null): String {
        return (if (number != null ) "`${number + 1}.` " else "") +
                "**[${track.info.title}](${track.info.uri})** " +
                "${track.info.author} - ${Duration.ofMillis(track.duration).toString().removePrefix("PT").toLowerCase()}"
    }
}
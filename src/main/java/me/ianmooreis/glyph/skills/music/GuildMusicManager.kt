package me.ianmooreis.glyph.skills.music

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import me.ianmooreis.glyph.extensions.reply
import net.dv8tion.jda.core.entities.Message
import java.time.Duration


class GuildMusicManager(manager: AudioPlayerManager) {
    private val player: AudioPlayer = manager.createPlayer()
    private val scheduler: TrackScheduler = TrackScheduler(player)
    val sendHandler: AudioPlayerSendHandler = AudioPlayerSendHandler(player)
    val nowPlaying: AudioTrack?
            get() = player.playingTrack

    init {
        player.addListener(scheduler)
    }

    fun skip() {
        scheduler.nextTrack()
    }

    fun stop() {
        player.destroy()
    }

    fun getQueue(): List<AudioTrack> {
        return scheduler.getQueue()
    }

    fun clearQueue() {
        scheduler.clearQueue()
    }

    fun queue(message: Message, identifier: String) {
        MusicSkillManager.playerManager.loadItemOrdered(this, identifier, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) {
                if (Duration.ofMillis(track.duration).toMinutes() < 60) {
                    scheduler.queue(track)
                    val playingIn = Duration.ofMillis(getQueue().map { it.duration }.sum()).toString().removePrefix("PT").toLowerCase()
                    message.reply(
                            "Adding to queue: ${track.info.title}\n" +
                            if (getQueue().isNotEmpty()) "Playing in: $playingIn" else "Playing now!")
                } else {
                    message.reply("I will only play songs less than 1 hour long!")
                }
            }

            override fun playlistLoaded(playlist: AudioPlaylist) {
                val tracks = playlist.tracks
                message.reply("Adding **" + tracks.size + "** tracks to queue from playlist: " + playlist.name)
                tracks.forEach { scheduler.queue(it) }
            }

            override fun noMatches() {
                message.reply("Nothing found by $identifier")
            }

            override fun loadFailed(exception: FriendlyException) {
                message.reply("Could not play: " + exception.message)
            }
        })
    }
}
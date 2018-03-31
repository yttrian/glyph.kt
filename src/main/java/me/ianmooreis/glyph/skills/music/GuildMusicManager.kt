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

    fun queue(message: Message, query: String) {
        MusicSkillManager.playerManager.loadItemOrdered(this, query, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) {
                if (Duration.ofMillis(track.duration).toMinutes() < 60) {
                    scheduler.queue(track)
                    message.reply(
                            "Adding to queue: ${track.info.title}\n" +
                            "Playing in: ${Duration.ofMillis(getQueue().map { it.duration }.sum()).toString().removePrefix("PT").toLowerCase()}")
                } else {
                    message.reply("I will only play songs less than 1 hour long!")
                }
            }

            override fun playlistLoaded(playlist: AudioPlaylist) {
                var firstTrack: AudioTrack? = playlist.selectedTrack
                val tracks = playlist.tracks

                if (firstTrack == null) {
                    firstTrack = playlist.tracks[0]
                }

                if (false) { //TODO: Check
                    message.reply("Adding **" + playlist.tracks.size + "** tracks to queue from playlist: " + playlist.name)
                    tracks.forEach { scheduler.queue(it) }
                } else {
                    message.reply("Adding to queue " + firstTrack!!.info.title + " (first track of playlist " + playlist.name + ")")
                    scheduler.queue(firstTrack)
                }
            }

            override fun noMatches() {
                message.reply("Nothing found by $query")
            }

            override fun loadFailed(exception: FriendlyException) {
                message.reply("Could not play: " + exception.message)
            }
        })
    }
}
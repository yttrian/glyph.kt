package me.ianmooreis.glyph.skills.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import java.util.*


class TrackScheduler(private val player: AudioPlayer) : AudioEventAdapter() {
    private val queue: Queue<AudioTrack> = LinkedList<AudioTrack>()

    fun queue(track: AudioTrack) {
        if (!player.startTrack(track, true)) {
            queue.offer(track)
        }
    }

    fun getQueue(): List<AudioTrack> {
        return queue.toList()
    }

    fun clearQueue() {
        queue.clear()
    }

    fun nextTrack() {
        player.startTrack(queue.poll(), false)
    }

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        if (endReason.mayStartNext) {
            nextTrack()
        }
    }
}
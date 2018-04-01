package me.ianmooreis.glyph.skills.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import sun.plugin2.message.Message

data class TrackRequest(val track: AudioTrack, val invoker: Message)
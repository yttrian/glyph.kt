package me.ianmooreis.glyph.skills.music

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import net.dv8tion.jda.core.entities.Guild

object MusicSkillManager {
    val playerManager = DefaultAudioPlayerManager()
    private val managers = mutableMapOf<Long, GuildMusicManager>()

    init {
        AudioSourceManagers.registerRemoteSources(playerManager)
    }

    fun getOrCreateManager(guild: Guild): GuildMusicManager {
        return managers.getOrPut(guild.idLong, { GuildMusicManager(playerManager) })
    }
}
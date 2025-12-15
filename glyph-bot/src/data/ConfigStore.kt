package org.yttr.glyph.bot.data

import net.dv8tion.jda.api.entities.Guild

interface ConfigStore {
    suspend fun getConfig(guild: Guild): ServerConfig
}

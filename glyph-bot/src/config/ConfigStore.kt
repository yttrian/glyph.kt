package org.yttr.glyph.shared.config

import net.dv8tion.jda.api.entities.Guild

interface ConfigStore {
    suspend fun getConfig(guild: Guild): ServerConfig
}

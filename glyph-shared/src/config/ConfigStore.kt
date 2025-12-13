package org.yttr.glyph.shared.config

import net.dv8tion.jda.api.entities.Guild
import org.yttr.glyph.shared.config.server.ServerConfig

interface ConfigStore {
    suspend fun getConfig(guild: Guild): ServerConfig
}

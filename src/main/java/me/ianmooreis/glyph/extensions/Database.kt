package me.ianmooreis.glyph.extensions

import me.ianmooreis.glyph.orchestrators.DatabaseOrchestrator
import me.ianmooreis.glyph.orchestrators.ServerConfig
import net.dv8tion.jda.core.entities.Guild

val Guild.config : ServerConfig
    get() = DatabaseOrchestrator.getServerConfig(this)

fun Guild.deleteConfig() {
    DatabaseOrchestrator.deleteServerConfig(this)
}
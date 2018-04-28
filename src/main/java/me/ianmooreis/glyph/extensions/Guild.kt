package me.ianmooreis.glyph.extensions

import me.ianmooreis.glyph.orchestrators.DatabaseOrchestrator
import me.ianmooreis.glyph.orchestrators.ServerConfig
import net.dv8tion.jda.core.entities.Guild

val Guild.config: ServerConfig
    get() = DatabaseOrchestrator.getServerConfig(this)

fun Guild.deleteConfig() {
    DatabaseOrchestrator.deleteServerConfig(this)
}

val Guild.isBotFarm: Boolean
    get() = (botRatio > .8 && members.count() > 10 && !DatabaseOrchestrator.hasCustomConfig(this))

val Guild.botRatio: Float
    get() {
        val members = this.members.count()
        val bots = this.members.count { it.user.isBot }
        return (bots.toFloat() / members.toFloat())
    }
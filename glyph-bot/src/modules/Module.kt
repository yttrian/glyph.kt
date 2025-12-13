package org.yttr.glyph.bot.modules

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction

interface Module {
    /**
     * Register listeners only. Implement [updateCommands] if you need to add commands.
     */
    fun boot(jda: JDA)

    fun updateCommands(commands: CommandListUpdateAction) = Unit
}

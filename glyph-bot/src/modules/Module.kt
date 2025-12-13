package org.yttr.glyph.bot.modules

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction

interface Module {
    /**
     * Use [commands] not [jda] to register commands!
     */
    fun register(jda: JDA, commands: CommandListUpdateAction)
}

package org.yttr.glyph.bot.modules

import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.CommandData

abstract class Module : ListenerAdapter() {
    private val commandListeners = mutableMapOf<String, (GenericCommandInteractionEvent) -> Unit>()

    abstract fun register()

    fun onCommand(name: String, function: (GenericCommandInteractionEvent) -> Unit) {
        commandListeners[name] = function
    }

    override fun onGenericCommandInteraction(event: GenericCommandInteractionEvent) {
        commandListeners[event.name]?.invoke(event)
    }

    open fun commands(): List<CommandData> = emptyList()
}

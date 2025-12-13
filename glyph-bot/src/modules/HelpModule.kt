package org.yttr.glyph.bot.modules

import dev.minn.jda.ktx.events.onCommand
import dev.minn.jda.ktx.interactions.commands.slash
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.reply_
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction

class HelpModule : Module {
    override fun register(jda: JDA, commands: CommandListUpdateAction) {
        commands.slash(name = "help", description = "Get help using Glyph")

        jda.onCommand(name = "help") { event ->
            help(event)
        }
    }

    fun help(event: GenericCommandInteractionEvent) {
        val name = event.jda.selfUser.name
        val embed = Embed {
            title = "Help"
            color = EMBED_COLOR
        }

        event.reply_(embeds = listOf(embed), ephemeral = true).queue()
    }

    companion object {
        private const val EMBED_COLOR = 0x4687E5
    }
}

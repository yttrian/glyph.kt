package org.yttr.glyph.bot.modules

import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.events.onCommand
import dev.minn.jda.ktx.interactions.commands.slash
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.MessageCreate
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction

class ObservatoryModule : Module {
    override fun boot(jda: JDA) {
        jda.onCommand("status") { event -> status(event) }
        jda.listener<GuildLeaveEvent> {}
        jda.listener<GuildJoinEvent> {}
    }

    override fun updateCommands(commands: CommandListUpdateAction) {
        commands.slash("status", "Check the operating status of Glyph")
    }

    private fun status(event: GenericCommandInteractionEvent) {
        event.reply(MessageCreate {
            embeds += Embed {
                title = "Glyph Status"
                field(name = "Ping", value = "${event.jda.gatewayPing} ms")
                field(name = "Servers", value = "${event.jda.guildCache.size()}")
            }
        })
    }
}

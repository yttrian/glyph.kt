package org.yttr.glyph.bot.modules

import dev.minn.jda.ktx.events.onCommand
import dev.minn.jda.ktx.interactions.commands.choice
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.interactions.commands.restrict
import dev.minn.jda.ktx.interactions.commands.slash
import dev.minn.jda.ktx.interactions.components.menu
import dev.minn.jda.ktx.interactions.components.replyModal
import dev.minn.jda.ktx.interactions.components.row
import dev.minn.jda.ktx.util.SLF4J
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction

class ConfigModule : Module {
    private val log by SLF4J

    override fun boot(jda: JDA) {
        jda.onCommand("config") { event ->
            modalConfig(event)
        }
    }

    override fun updateCommands(commands: CommandListUpdateAction) {
        commands.slash(name = "config", description = "Configure Glyph") {
            restrict(guild = true, perm = Permission.MANAGE_SERVER)

            option<String>(name = "feature", description = "The feature to configure", required = true) {
                choice("Starboard", "starboard")
                choice("QuickView", "quickview")
            }
        }
    }

    private fun modalConfig(event: GenericCommandInteractionEvent) {
        event.replyModal("config", "Configure Glyph") {
            // Uh oh, I need Components V2 I fear
            components += row(EntitySelectMenu.SelectTarget.CHANNEL.menu("test"))
        }.queue()
    }
}

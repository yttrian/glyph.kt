package org.yttr.glyph.bot.modules

import dev.minn.jda.ktx.events.onCommand
import dev.minn.jda.ktx.interactions.commands.slash
import dev.minn.jda.ktx.interactions.components.replyModal
import dev.minn.jda.ktx.util.SLF4J
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction

class ConfigModule : Module {
    private val log by SLF4J

    override fun boot(jda: JDA) {
        jda.onCommand("config") { event ->
            event.reply("Visit https://glyph.yttr.dev/config to configure Glyph.").setEphemeral(true).queue()
        }
    }

    override fun updateCommands(commands: CommandListUpdateAction) {
        commands.slash("config", "Configure Glyph") {
            defaultPermissions = DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER)
            setContexts(InteractionContextType.GUILD)
        }
    }

    private fun modalConfig(event: GenericCommandInteractionEvent) {
        event.replyModal("config", "Configure Glyph") {

        }
    }
}

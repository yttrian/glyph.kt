package org.yttr.glyph.bot.modules

import dev.minn.jda.ktx.events.onCommand
import dev.minn.jda.ktx.interactions.commands.slash
import dev.minn.jda.ktx.interactions.components.link
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.MessageCreate
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import org.yttr.glyph.bot.Resources

class HelpModule : Module {
    override fun boot(jda: JDA) {
        jda.onCommand("help") { event -> help(event) }
    }

    override fun updateCommands(commands: CommandListUpdateAction) {
        commands.slash("help", "Get help using Glyph")
    }

    fun help(event: GenericCommandInteractionEvent) {
        val name = event.jda.selfUser.name

        val message = MessageCreate {
            embeds += Embed {
                title = "$name Help"
                color = EMBED_COLOR
                description = Resources.readText("help.md").format(name)
            }

            components += ActionRow.of(
                link("https://glyph.yttr.org/skills", "Skills", Emoji.fromUnicode("🕺")),
                link("https://glyph.yttr.org/config", "Configure", Emoji.fromUnicode("⚙️")),
                link("https://ko-fi.com/throudin", "Buy me a Ko-fi", Emoji.fromUnicode("☕"))
            )
        }

        event.reply(message).setEphemeral(true).queue()
    }

    companion object {
        private const val EMBED_COLOR = 0x4687E5
    }
}

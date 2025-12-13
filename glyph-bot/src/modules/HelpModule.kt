package org.yttr.glyph.bot.modules

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.components.buttons.Button
import net.dv8tion.jda.api.components.buttons.ButtonStyle
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import org.yttr.glyph.bot.Resources
import org.yttr.glyph.bot.jda.buildReply

class HelpModule : Module() {
    override fun register() {
        onCommand("help") { event -> help(event) }
    }

    override fun commands() = listOf(
        Commands.slash("help", "Get help using Glyph")
    )

    fun help(event: GenericCommandInteractionEvent) {
        val name = event.jda.selfUser.name

        event.buildReply {
            ephemeral = true

            embeds += EmbedBuilder()
                .setTitle("$name Help")
                .setColor(EMBED_COLOR)
                .setDescription(Resources.readText("help.md").format(name))
                .build()

            components += ActionRow.of(
                linkButton("https://glyph.yttr.org/skills", "Skills", "🕺"),
                linkButton("https://glyph.yttr.org/config", "Configure", "⚙️"),
                linkButton("https://ko-fi.com/throudin", "Buy me a Ko-fi", "☕")
            )
        }.queue()
    }

    companion object {
        private const val EMBED_COLOR = 0x4687E5

        private fun linkButton(url: String, label: String, emoji: String) =
            Button.of(ButtonStyle.LINK, url, label, Emoji.fromUnicode(emoji))
    }
}

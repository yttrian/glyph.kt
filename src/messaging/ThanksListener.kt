package org.yttr.glyph.messaging

import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import org.yttr.glyph.extensions.embedBuilder
import org.yttr.glyph.readMarkdown

object ThanksListener : ListenerAdapter() {
    private const val COMMAND_NAME = "thanks"

    override fun onReady(event: ReadyEvent) {
        val commandData = CommandData(COMMAND_NAME, "Special thanks to our sponsors.")

        event.jda.upsertCommand(commandData).queue()
    }

    private val content = this::class.java.classLoader.readMarkdown("thanks.md")

    override fun onSlashCommand(event: SlashCommandEvent) {
        if (event.commandPath == COMMAND_NAME) {
            val embed = embedBuilder {
                setTitle("Acknowledgements")
                setDescription(content)
            }

            event.replyEmbeds(embed).setEphemeral(true).queue()
        }
    }
}

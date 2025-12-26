package org.yttr.glyph.bot.modules

import dev.minn.jda.ktx.events.onCommand
import dev.minn.jda.ktx.events.onCommandAutocomplete
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.interactions.commands.slash
import dev.minn.jda.ktx.messages.Embed
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.CommandAutoCompleteInteraction
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import org.jsoup.Jsoup
import org.yttr.glyph.bot.wrappers.Wikipedia

class WikipediaModule(private val wikipedia: Wikipedia = Wikipedia()) : Module {
    override fun boot(jda: JDA) {
        jda.onCommand("wikipedia") { event -> summarizeArticle(event) }
        jda.onCommandAutocomplete("wikipedia", "article") { event -> autoCompleteArticle(event) }
    }

    override fun updateCommands(commands: CommandListUpdateAction) {
        commands.slash(name = "wikipedia", description = "Search Wikipedia") {
            option<Long>(
                name = "article",
                description = "The article to retrieve the summary for",
                required = true,
                autocomplete = true
            )
        }
    }

    private suspend fun autoCompleteArticle(event: CommandAutoCompleteInteraction) {
        val partialQuery = event.focusedOption.value

        val results = wikipedia.search(partialQuery, limit = MAX_RESULTS)

        val choices = results.map { result ->
            Command.Choice(result.title, result.pageId)
        }

        event.replyChoices(choices).queue()
    }

    private suspend fun summarizeArticle(event: GenericCommandInteractionEvent) {
        val result = event.getOption("article")?.asLong?.let { wikipedia.extract(it) }

        if (result == null) {
            event.reply("Unable to find a matching article!").setEphemeral(true).queue()
            return
        }

        val embed = Embed {
            title = result.title
            description = Jsoup.parse(result.extract).text().let { extract ->
                extract.take(MAX_LENGTH) + if (extract.length > MAX_LENGTH) "..." else ""
            }
            url = "https://en.wikipedia.org/wiki/${result.title.replace(" ", "_")}"

            footer {
                name = "Wikipedia"
            }
        }

        event.replyEmbeds(embed).queue()
    }

    companion object {
        private const val MAX_RESULTS = 10
        private const val MAX_LENGTH = 512
    }
}

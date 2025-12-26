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
import net.dv8tion.jda.api.utils.MarkdownUtil
import org.jsoup.Jsoup
import org.yttr.glyph.bot.wrappers.Wikipedia
import java.time.Instant

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
//        event.deferReply().queue()

        val result = event.getOption("article")?.asLong?.let { wikipedia.extract(it) }

        if (result == null) {
            event.reply("Unable to find a matching article!").setEphemeral(true).queue()
            return
        }

        val embed = Embed {
            title = result.title
            description = formatDescription(result.extract).let { extract ->
                extract.take(MAX_LENGTH).substringBeforeLast(" ", "") + if (extract.length > MAX_LENGTH) "..." else ""
            }
            url = "https://en.wikipedia.org/wiki/${result.title.replace(" ", "_")}"
            timestamp = Instant.now()

            footer {
                name = "Wikipedia"
            }
        }

        event.replyEmbeds(embed).queue()
    }

    private fun formatDescription(extract: String): String {
        val soup = Jsoup.parse(extract)

        for (link in soup.select("a")) {
            link.text(MarkdownUtil.maskedLink(link.text(), link.attr("href")))
        }

        for (bold in soup.select("b")) {
            bold.text(MarkdownUtil.bold(bold.text()))
        }

        for (italic in soup.select("i")) {
            italic.text(MarkdownUtil.italics(italic.text()))
        }

        for (sup in soup.select("sup")) {
            sup.prependText("^")
        }

        return soup.text()
    }

    companion object {
        private const val MAX_RESULTS = 10
        private const val MAX_LENGTH = 512
    }
}

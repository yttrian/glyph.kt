package org.yttr.glyph.bot.skills.util

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Emoji
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.Button
import net.dv8tion.jda.api.interactions.components.ButtonStyle
import org.yttr.glyph.bot.Glyph
import org.yttr.glyph.bot.ai.AIResponse
import org.yttr.glyph.bot.extensions.asPlainMention
import org.yttr.glyph.bot.messaging.Response
import org.yttr.glyph.bot.skills.Skill
import org.yttr.glyph.shared.readMarkdown
import java.awt.Color

/**
 * A skill that shows users a help message
 */
class HelpSkill : Skill("skill.help") {
    override suspend fun onTrigger(event: MessageReceivedEvent, ai: AIResponse): Response {
        val name = event.jda.selfUser.name
        val creator = event.jda.retrieveUserById(Glyph.conf.getLong("management.creator-id")).await()
        val embed = EmbedBuilder()
            .setTitle("$name Help")
            .setDescription(helpTemplate.format(name, creator.asPlainMention))
            .setColor(embedColor)
            .build()

        return Response.Volatile(embed, actionRow)
    }

    companion object {
        private val helpTemplate = this::class.java.classLoader.readMarkdown("help.md") ?: "There is no help."
        private val embedColor = Color.decode("#4687E5")

        private fun linkButton(url: String, label: String, emoji: String) =
            Button.of(ButtonStyle.LINK, url, label, Emoji.fromUnicode(emoji))

        private val actionRow = ActionRow.of(
            linkButton("https://glyph.yttr.org/skills", "Skills", "🕺"),
            linkButton("https://glyph.yttr.org/config", "Configure", "⚙️"),
            linkButton("https://ko-fi.com/throudin", "Buy me a Ko-fi", "☕")
        )
    }
}

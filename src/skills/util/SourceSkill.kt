package org.yttr.glyph.bot.skills.util

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.yttr.glyph.bot.Glyph
import org.yttr.glyph.bot.ai.AIResponse
import org.yttr.glyph.bot.messaging.Response
import org.yttr.glyph.bot.skills.Skill
import java.awt.Color
import java.time.Instant

/**
 * A skill that allows users to see the license and link to the source code
 */
class SourceSkill : Skill("skill.source") {
    override suspend fun onTrigger(event: MessageReceivedEvent, ai: AIResponse): Response {
        val name = event.jda.selfUser.name
        val embed = EmbedBuilder()
            .setTitle("$name Source")
            .setDescription(ai.result.fulfillment.speech)
            .setFooter("$name-Kotlin-${Glyph.version}", null)
            .setTimestamp(Instant.now())
            .setColor(Color.getHSBColor(0.6f, 0.89f, 0.61f))
            .build()

        return Response.Volatile(embed)
    }
}

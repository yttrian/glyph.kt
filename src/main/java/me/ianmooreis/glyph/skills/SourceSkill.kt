package me.ianmooreis.glyph.skills

import ai.api.model.AIResponse
import me.ianmooreis.glyph.Glyph
import me.ianmooreis.glyph.extensions.reply
import me.ianmooreis.glyph.orchestrators.skills.Skill
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.awt.Color
import java.time.Instant

/**
 * A skill that allows users to see the license and link to the source code
 */
object SourceSkill : Skill("skill.source") {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        val name = event.jda.selfUser.name
        val embed = EmbedBuilder()
            .setTitle("$name Source")
            .setDescription(ai.result.fulfillment.speech.replace("\\n", "\n", true))
            .setFooter("$name-Kotlin-${Glyph.version}", null)
            .setTimestamp(Instant.now())
            .setColor(Color.getHSBColor(0.6f, 0.89f, 0.61f))
            .build()
        event.message.reply(embed = embed)
    }
}
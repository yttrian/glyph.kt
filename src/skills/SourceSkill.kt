package org.yttr.glyph.skills

import dev.kord.common.Color
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.message.embed
import kotlinx.datetime.Clock
import org.yttr.glyph.Glyph
import org.yttr.glyph.ai.AIResponse

/**
 * A skill that allows users to see the license and link to the source code
 */
class SourceSkill : Skill("skill.source") {
    override suspend fun perform(event: MessageCreateEvent, ai: AIResponse) {
        val name = event.kord.getSelf().username

        event.reply {
            embed {
                title = "$name Source"
                description = ai.result.fulfillment.speech
                footer {
                    text = "$name-Kotlin-${Glyph.version}"
                }
                color = Color(rgb = 0x11499c)
                timestamp = Clock.System.now()
            }
        }
    }
}

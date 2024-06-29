package org.yttr.glyph.skills

import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.message.embed
import org.yttr.glyph.ai.AIResponse

object ThanksSkill : Skill("skill.thanks") {
    private val thanks = this::class.java.classLoader.readMarkdown("thanks.md")

    override suspend fun perform(event: MessageCreateEvent, ai: AIResponse) {
        event.reply {
            embed {
                title = "Acknowledgements"
                description = thanks
            }
        }
    }
}

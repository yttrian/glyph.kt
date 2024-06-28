package org.yttr.glyph.skills

import dev.kord.core.behavior.reply
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.message.create.UserMessageCreateBuilder
import org.yttr.glyph.ai.AIResponse

/**
 * The definition of a skill with a trigger word, cooldown times, required permissions, and usage limits
 */
abstract class Skill(
    /**
     * The trigger world (a DialogFlow action) to use to refer to the skill
     */
    val trigger: String,
    val creatorOnly: Boolean = false
) {
    abstract suspend fun perform(event: MessageCreateEvent, ai: AIResponse)

    suspend fun MessageCreateEvent.reply(builder: UserMessageCreateBuilder.() -> Unit) = message.reply(builder)
}

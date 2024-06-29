package org.yttr.glyph.skills

import arrow.core.raise.nullable
import dev.kord.common.entity.Snowflake
import dev.kord.core.event.message.MessageCreateEvent
import org.yttr.glyph.ai.AIResponse

/**
 * A skill that allows users to send anonymous feedback via the global log webhook
 */
object FeedbackSkill : Skill("skill.feedback") {
    override suspend fun perform(event: MessageCreateEvent, ai: AIResponse) {
        // TODO: event.jda.selfUser.log("Feedback", "```${ai.result.getStringParameter("feedback")}```")

        nullable {
            val webhook = event.kord.getWebhookOrNull(Snowflake(conf.getLong("logging-webhook"))).bind()
        }

        event.reply {
            ai.result.fulfillment.speech
        }
    }
}

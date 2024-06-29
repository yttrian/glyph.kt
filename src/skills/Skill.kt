package org.yttr.glyph.skills

import com.typesafe.config.Config
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.reply
import dev.kord.core.entity.User
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.message.create.UserMessageCreateBuilder
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.yttr.glyph.ai.AIResponse
import org.yttr.glyph.data.RedisAsync

/**
 * A skill triggered by a natural language response.
 */
abstract class Skill(
    /**
     * The trigger world (a DialogFlow action) to use to refer to the skill
     */
    val trigger: String,
    val creatorOnly: Boolean = false
) : KoinComponent {
    val conf by inject<Config>()
    val redis by inject<RedisAsync>()

    abstract suspend fun perform(event: MessageCreateEvent, ai: AIResponse)

    /**
     * Is this user the bot's creator?
     */
    val User.isCreator
        get() = this.id == Snowflake(conf.getLong("creator-id"))

    /**
     * Shorthand for `event.message.reply`
     */
    suspend fun MessageCreateEvent.reply(builder: UserMessageCreateBuilder.() -> Unit) = message.reply(builder)
}

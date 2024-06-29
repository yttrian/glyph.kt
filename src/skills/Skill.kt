package org.yttr.glyph.skills

import com.typesafe.config.Config
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.reply
import dev.kord.core.entity.User
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.message.create.UserMessageCreateBuilder
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.ocpsoft.prettytime.PrettyTime
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
) : KoinComponent {
    val conf by inject<Config>()

    abstract suspend fun perform(event: MessageCreateEvent, ai: AIResponse)

    suspend fun MessageCreateEvent.reply(builder: UserMessageCreateBuilder.() -> Unit) = message.reply(builder)

    /**
     * Is this user the bot's creator?
     */
    val User.isCreator
        get() = this.id == Snowflake(conf.getLong("creator-id"))

    /**
     * Format with PrettyTime
     */
    fun Instant.formatPrettyTime(): String = PrettyTime(this.toJavaInstant()).format(java.time.Instant.now())
}

package org.yttr.glyph.skills

import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import dev.kord.common.Color
import dev.kord.common.entity.Snowflake
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.message.embed
import org.yttr.glyph.SimpleDescriptionBuilder
import org.yttr.glyph.ai.AIResponse

/**
 * A skill that allows users to get a timestamp from a Discord snowflake id
 */
object SnowstampSkill : Skill("skill.snowstamp") {
    override suspend fun perform(event: MessageCreateEvent, ai: AIResponse) {
        either {
            val snowflake = ensureNotNull(ai.result.getStringParameter("snowflake")) {
                "No snowflake provided!"
            }

            Snowflake(ensureNotNull(snowflake.toLongOrNull()) {
                "$snowflake could not be converted to a snowflake!"
            })
        }.onLeft {
            event.reply { content = it }
        }.onRight { snowflake ->
            event.reply {
                embed {
                    title = snowflake.value.toString()
                    description = SimpleDescriptionBuilder {
                        addField("UTC", snowflake.timestamp.toString())
                        addField("UNIX", snowflake.timestamp.toEpochMilliseconds())
                    }
                    color = Color(rgb = 0xFFFFFF)
                    footer {
                        text = "Snowstamp"
                    }
                    timestamp = snowflake.timestamp
                }
            }
        }
    }
}

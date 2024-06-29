package org.yttr.glyph.skills

import dev.kord.common.Color
import dev.kord.common.entity.Snowflake
import dev.kord.core.builder.components.emoji
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.message.actionRow
import dev.kord.rest.builder.message.embed
import org.yttr.glyph.ai.AIResponse
import org.yttr.glyph.readMarkdown

/**
 * A skill that shows users a help message
 */
class HelpSkill : Skill("skill.help") {
    override suspend fun perform(event: MessageCreateEvent, ai: AIResponse) {
        val name = event.kord.getSelf().username
        val creator = event.kord.getUser(Snowflake(conf.getLong("creator-id")))

        event.reply {
            embed {
                title = "$name Help"
                description = helpTemplate.format(name, creator)
                color = embedColor
            }

            actionRow {
                linkButton("https://gl.yttr.org/skills") {
                    label = "Skills"
                    emoji(ReactionEmoji.Unicode("🕺"))
                }
                linkButton("https://gl.yttr.org/config") {
                    label = "Configure"
                    emoji(ReactionEmoji.Unicode("⚙️"))
                }
                linkButton("https://ko-fi.com/throudin") {
                    label = "Buy me a Ko-fi"
                    emoji(ReactionEmoji.Unicode("☕"))
                }
            }
        }
    }

    companion object {
        private val helpTemplate = this::class.java.classLoader.readMarkdown("help.md") ?: "There is no help."
        private val embedColor = Color(rgb = 0x4687E5)
    }
}

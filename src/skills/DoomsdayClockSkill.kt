package org.yttr.glyph.skills

import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.message.embed
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.utils.io.core.use
import kotlinx.datetime.Clock
import org.yttr.glyph.ai.AIResponse

/**
 * A skill that allows users to see the current Doomsday Clock status
 */
class DoomsdayClockSkill : Skill("skill.doomsday_clock") {
    private val timeRegex = Regex(
        pattern = "(IT IS (.*?) TO MIDNIGHT)",
        option = RegexOption.IGNORE_CASE
    )

    private val reasonRegex = Regex(
        pattern = "<div class=\"uabb-infobox-text uabb-text-editor\"><p>(.*)(?:See the|Read the)",
        option = RegexOption.IGNORE_CASE
    )

    override suspend fun perform(event: MessageCreateEvent, ai: AIResponse) {
        return HttpClient().use { client ->
            try {
                val content = client.get("https://thebulletin.org/timeline").bodyAsText()

                event.reply {
                    embed {
                        title = timeRegex.findAll(content).first().groups[1]?.value ?: "Unknown"
                        url = "https://thebulletin.org/timeline"
                        description = reasonRegex.find(content)?.groups?.get(2)?.value
                        footer {
                            text = "Doomsday Clock"
                        }
                        timestamp = Clock.System.now()
                    }
                }
            } catch (cause: Throwable) {
                event.reply {
                    content = "I was unable to check the Doomsday Clock!"
                }
            }
        }
    }
}

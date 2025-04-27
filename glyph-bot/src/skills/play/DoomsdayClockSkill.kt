package org.yttr.glyph.bot.skills.play

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.yttr.glyph.bot.ai.AIResponse
import org.yttr.glyph.bot.messaging.Response
import org.yttr.glyph.bot.skills.Skill
import java.time.Instant

/**
 * A skill that allows users to see the current Doomsday Clock status
 */
class DoomsdayClockSkill : Skill("skill.doomsday_clock") {
    private val timeRegex = Regex("(IT IS (.*?) TO MIDNIGHT)", RegexOption.IGNORE_CASE)
    private val reasonRegex =
        Regex("<div class=\"uabb-infobox-text uabb-text-editor\"><p>(.*)(?:See the|Read the)", RegexOption.IGNORE_CASE)

    override suspend fun onTrigger(event: MessageReceivedEvent, ai: AIResponse): Response {
        return HttpClient().use { client ->
            try {
                val content = client.get("https://thebulletin.org/timeline").body<String>()

                val minutesToMidnight = timeRegex.findAll(content).first().groups[1]?.value ?: "Unknown"
                val reason = reasonRegex.find(content)?.groups?.get(2)?.value

                Response.Volatile(
                    EmbedBuilder()
                        .setTitle(minutesToMidnight, "https://thebulletin.org/timeline")
                        .setDescription(reason)
                        .setFooter("Doomsday Clock", null)
                        .setTimestamp(Instant.now())
                        .build()
                )
            } catch (cause: Throwable) {
                Response.Volatile("I was unable to check the Doomsday Clock!")
            }
        }
    }
}

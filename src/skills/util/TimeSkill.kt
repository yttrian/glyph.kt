package org.yttr.glyph.skills.util

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.yttr.glyph.ai.AIResponse
import org.yttr.glyph.messaging.Response
import org.yttr.glyph.skills.Skill
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.TimeZone

/**
 * A skill that attempts to show the time in other timezones
 */
class TimeSkill : Skill("skill.time") {
    override suspend fun onTrigger(event: MessageReceivedEvent, ai: AIResponse): Response {
        val df = SimpleDateFormat("**HH:mm:ss** 'on' EEEE, MMMM dd, yyyy")
        df.timeZone = TimeZone.getTimeZone(ai.result.getStringParameter("timezone"))

        return Response.Volatile(
            EmbedBuilder()
                .setTitle(df.timeZone.displayName)
                .setDescription(df.format(Date()))
                .setFooter("Time", null)
                .setTimestamp(Instant.now())
                .build()
        )
    }
}

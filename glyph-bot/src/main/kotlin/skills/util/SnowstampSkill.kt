package org.yttr.glyph.bot.skills.util

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.utils.TimeUtil
import org.yttr.glyph.bot.ai.AIResponse
import org.yttr.glyph.bot.directors.messaging.SimpleDescriptionBuilder
import org.yttr.glyph.bot.messaging.Response
import org.yttr.glyph.bot.skills.Skill
import java.awt.Color

/**
 * A skill that allows users to get a timestamp from a Discord snowflake id
 */
class SnowstampSkill : Skill("skill.snowstamp") {
    override suspend fun onTrigger(event: MessageReceivedEvent, ai: AIResponse): Response.Volatile {
        val snowflake = ai.result.getStringParameter("snowflake") ?: ""
        val snowflakeId = try {
            snowflake.toLong()
        } catch (e: NumberFormatException) {
            return Response.Volatile("`$snowflake` is not a snowflake!")
        }
        val snowflakeInstant = TimeUtil.getTimeCreated(snowflakeId).toInstant()
        val description = SimpleDescriptionBuilder()
            .addField("UTC", snowflakeInstant.toString())
            .addField("UNIX", snowflakeInstant.toEpochMilli())
            .build()
        return Response.Volatile(
            EmbedBuilder()
                .setTitle(snowflakeId.toString())
                .setDescription(description)
                .setColor(Color.WHITE)
                .setFooter("Snowstamp", null)
                .setTimestamp(snowflakeInstant)
                .build()
        )
    }
}

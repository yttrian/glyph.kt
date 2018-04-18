package me.ianmooreis.glyph.skills

import ai.api.model.AIResponse
import me.ianmooreis.glyph.extensions.reply
import me.ianmooreis.glyph.orchestrators.SkillAdapter
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.awt.Color
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

object SnowstampSkill : SkillAdapter("skill.snowstamp") {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        val snowflake = ai.result.getStringParameter("snowflake").toLong()
        val milliseconds = (snowflake / 4194304 + 1420070400000)
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        event.message.reply(EmbedBuilder()
                .setTitle(snowflake.toString())
                .setDescription("**UTC** ${sdf.format(Date(milliseconds))}\n**UNIX** $milliseconds")
                .setColor(Color.WHITE)
                .setFooter("Snowstamp", null)
                .setTimestamp(Instant.ofEpochMilli(milliseconds))
                .build())
    }
}
package me.ianmooreis.glyph.skills

import ai.api.model.AIResponse
import me.ianmooreis.glyph.extensions.reply
import me.ianmooreis.glyph.orchestrators.SkillAdapter
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.utils.MiscUtil
import java.awt.Color

object SnowstampSkill : SkillAdapter("skill.snowstamp") {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        val snowflake = ai.result.getStringParameter("snowflake").toLong()
        val snowflakeInstant = MiscUtil.getCreationTime(snowflake).toInstant()
        event.message.reply(EmbedBuilder()
                .setTitle(snowflake.toString())
                .setDescription("**UTC** $snowflakeInstant\n**UNIX** ${snowflakeInstant.toEpochMilli()}")
                .setColor(Color.WHITE)
                .setFooter("Snowstamp", null)
                .setTimestamp(snowflakeInstant)
                .build())
    }
}
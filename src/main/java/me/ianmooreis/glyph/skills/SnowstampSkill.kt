package me.ianmooreis.glyph.skills

import ai.api.model.AIResponse
import me.ianmooreis.glyph.extensions.reply
import me.ianmooreis.glyph.orchestrators.messaging.CustomEmote
import me.ianmooreis.glyph.orchestrators.messaging.SimpleDescriptionBuilder
import me.ianmooreis.glyph.orchestrators.skills.SkillAdapter
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.utils.MiscUtil
import java.awt.Color

object SnowstampSkill : SkillAdapter("skill.snowstamp") {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        val snowflake = ai.result.getStringParameter("snowflake")
        val snowflakeId = try {
            snowflake.toLong()
        } catch (e: NumberFormatException) {
            event.message.reply("${CustomEmote.XMARK} `$snowflake` is not a snowflake!")
            return
        }
        val snowflakeInstant = MiscUtil.getCreationTime(snowflakeId).toInstant()
        val description = SimpleDescriptionBuilder()
                .addField("UTC", snowflakeInstant.toString())
                .addField("UNIX", snowflakeInstant.toEpochMilli())
                .build()
        event.message.reply(EmbedBuilder()
                .setTitle(snowflakeId.toString())
                .setDescription(description)
                .setColor(Color.WHITE)
                .setFooter("Snowstamp", null)
                .setTimestamp(snowflakeInstant)
                .build())
    }
}
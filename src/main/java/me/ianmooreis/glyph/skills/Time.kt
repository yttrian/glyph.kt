package me.ianmooreis.glyph.skills

import ai.api.model.AIResponse
import me.ianmooreis.glyph.orchestrators.Skill
import me.ianmooreis.glyph.orchestrators.reply
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

object TimeSkill : Skill("skill.time") {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        val df = SimpleDateFormat("**HH:mm:ss** 'on' EEEE, MMMM dd, yyyy")
        df.timeZone = TimeZone.getTimeZone(ai.result.getStringParameter("timezone"))
        event.message.reply(EmbedBuilder()
                .setTitle(df.timeZone.displayName)
                .setDescription(df.format(Date()))
                .setFooter("Time", null)
                .setTimestamp(Instant.now())
                .build())
    }
}
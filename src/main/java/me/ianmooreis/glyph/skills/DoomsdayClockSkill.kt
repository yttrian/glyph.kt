package me.ianmooreis.glyph.skills

import ai.api.model.AIResponse
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import me.ianmooreis.glyph.extensions.reply
import me.ianmooreis.glyph.orchestrators.SkillAdapter
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.time.Instant

object DoomsdayClockSkill : SkillAdapter("skill.doomsday_clock") {
    private val timeRegex = Regex("(IT IS (.*?) TO MIDNIGHT)", RegexOption.IGNORE_CASE)
    private val reasonRegex = Regex("<div class=\"body-text\"><span class=\"timeline-year\">(\\d{4})</span>:(.*)(See the|Read the)", RegexOption.IGNORE_CASE)

    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        "https://thebulletin.org/timeline".httpGet().responseString { _, _, result ->
            when (result) {
                is Result.Success -> {
                    val content = result.get()
                    val minutesToMidnight = timeRegex.findAll(content).drop(1).first().groups[1]?.value ?: "Unknown"
                    val reason = reasonRegex.find(content)?.groups?.get(2)?.value
                    event.message.reply(EmbedBuilder()
                            .setTitle(minutesToMidnight, "https://thebulletin.org/timeline")
                            .setDescription(reason)
                            .setFooter("Doomsday Clock", null)
                            .setTimestamp(Instant.now())
                            .build())
                }
                is Result.Failure -> {
                    event.message.reply("I was unable to check the Doomsday Clock!")
                }
            }
        }
    }
}
package me.ianmooreis.glyph.skills

import ai.api.model.AIResponse
import com.google.gson.JsonObject
import me.ianmooreis.glyph.extensions.reply
import me.ianmooreis.glyph.orchestrators.SkillAdapter
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.time.Instant
import java.util.concurrent.TimeUnit

object EphemeralSaySkill : SkillAdapter("skill.ephemeral_say", requiredPermissionsSelf = listOf(Permission.MESSAGE_MANAGE), guildOnly = true) {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        val durationEntity: JsonObject? = ai.result.getComplexParameter("duration")
        if (durationEntity == null) {
            event.message.reply("That is an invalid time duration, specify how many seconds you want your message to last.", deleteAfterDelay = 5, deleteAfterUnit = TimeUnit.SECONDS)
            return
        }

        val durationAmount = durationEntity.get("amount").asLong
        val durationUnit = when (durationEntity.get("unit").asString) {
            "s" -> TimeUnit.SECONDS
            null -> null
            else -> null
        }
        if (durationUnit == null || durationAmount > 30) {
            event.message.reply("You can only say something ephemerally for less than 30 seconds!", deleteAfterDelay = 5, deleteAfterUnit = TimeUnit.SECONDS)
            return
        } else if (durationAmount <= 0) {
            event.message.reply("You can only say something ephemerally for a positive amount of time!", deleteAfterDelay = 5, deleteAfterUnit = TimeUnit.SECONDS)
            return
        }

        event.message.delete().reason("Ephemeral Say").queue()
        event.message.reply(EmbedBuilder()
                .setAuthor(event.author.name, null, event.author.avatarUrl)
                .setDescription(ai.result.getStringParameter("message"))
                .setFooter("Ephemeral Say", null)
                .setTimestamp(Instant.now().plus(durationAmount, durationUnit.toChronoUnit()))
                .build(),
                deleteAfterDelay = durationAmount, deleteAfterUnit = durationUnit)
    }
}
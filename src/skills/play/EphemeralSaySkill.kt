package org.yttr.glyph.skills.play

import com.google.gson.JsonObject
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.yttr.glyph.ai.AIResponse
import org.yttr.glyph.messaging.Response
import org.yttr.glyph.skills.Skill
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Allows users to briefly say something before it is deleted automatically
 */
class EphemeralSaySkill :
    Skill("skill.ephemeral_say", requiredPermissionsSelf = listOf(Permission.MESSAGE_MANAGE), guildOnly = true) {
    override suspend fun onTrigger(event: MessageReceivedEvent, ai: AIResponse): Response {
        val durationEntity: JsonObject = ai.result.getComplexParameter("duration")
            ?: return Response.Ephemeral(
                "That is an invalid time duration, specify how many seconds you want your message to last.",
                Duration.ofSeconds(5)
            )

        val durationAmount = durationEntity.get("amount").asLong
        val durationUnit = when (durationEntity.get("unit").asString) {
            "s" -> ChronoUnit.SECONDS
            else -> null
        }
        if (durationUnit == null || durationAmount > 30) {
            return Response.Ephemeral(
                "You can only say something ephemerally for less than 30 seconds!",
                Duration.ofSeconds(5)
            )
        } else if (durationAmount <= 0) {
            return Response.Ephemeral(
                "You can only say something ephemerally for a positive amount of time!",
                Duration.ofSeconds(5)
            )
        }

        event.message.delete().reason("Ephemeral Say").queue()
        return Response.Ephemeral(
            EmbedBuilder()
                .setAuthor(event.author.name, null, event.author.avatarUrl)
                .setDescription(ai.result.getStringParameter("message"))
                .setFooter("Ephemeral Say", null)
                .setTimestamp(Instant.now().plus(durationAmount, durationUnit))
                .build(),
            Duration.of(durationAmount, durationUnit)
        )
    }
}

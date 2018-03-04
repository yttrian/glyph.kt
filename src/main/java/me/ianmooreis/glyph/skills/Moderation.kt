package me.ianmooreis.glyph.skills

import ai.api.model.AIResponse
import com.google.gson.JsonObject
import me.ianmooreis.glyph.extensions.getMessagesSince
import me.ianmooreis.glyph.extensions.getinfoEmbed
import me.ianmooreis.glyph.extensions.reply
import me.ianmooreis.glyph.extensions.toDate
import me.ianmooreis.glyph.orchestrators.CustomEmote
import me.ianmooreis.glyph.orchestrators.Skill
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import org.ocpsoft.prettytime.PrettyTime
import java.time.Instant

object PurgeSkill : Skill("skill.moderation.purge", serverOnly = true, requiredPermissionsUser = listOf(Permission.MESSAGE_MANAGE)) {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        val time = event.message.creationTime
        val durationEntity: JsonObject? = ai.result.getComplexParameter("duration")

        if (!event.guild.selfMember.hasPermission(Permission.MESSAGE_MANAGE)) {
            event.message.reply("I need permission to Manage Messages in order to purge messages!")
            return
        }
        if (durationEntity == null) {
            event.message.reply("That is an invalid time duration, try being less vague with abbreviations.")
            return
        }

        val durationAmount = durationEntity.get("amount").asLong
        val durationUnit = durationEntity.get("unit").asString
        val duration = when (durationUnit) {
            "wk" -> time.minusWeeks(durationAmount)
            "day" -> time.minusDays(durationAmount)
            "h" -> time.minusHours(durationAmount)
            "min" -> time.minusMinutes(durationAmount)
            "s" -> time.minusSeconds(durationAmount)
            null -> time
            else -> time
        }
        if (duration.isBefore(time.minusDays(14))) {
            event.message.reply("You can only purge up to 14 days!")
            return
        }

        val prettyDuration = PrettyTime().format(duration.toDate())
        event.message.addReaction(CustomEmote.LOADING.emote).queue()
        event.message.reply(EmbedBuilder()
                .setTitle("Purge Started")
                .setDescription("${CustomEmote.NOMARK} Purging all messages since $prettyDuration!")
                .setFooter("Moderation", null)
                .setTimestamp(Instant.now())
                .build())
        val messages = event.textChannel.getMessagesSince(duration)
        try {
            event.textChannel.deleteMessages(messages).queue {
                event.message.reply(EmbedBuilder()
                        .setTitle("Purge Completed")
                        .setDescription("${CustomEmote.CHECKMARK} Purged ${messages.size} messages since $prettyDuration!")
                        .setFooter("Moderation", null)
                        .setTimestamp(Instant.now())
                        .build(), deleteAfterDelay = 10)
            }
        } catch (e: IllegalArgumentException) {
            event.message.reply("${CustomEmote.XMARK} An error occurred trying to find those message to purge.")
        }
    }
}

object UserInfoSkill : Skill("skill.moderation.user_info") { //TODO: Change to camelcase before release
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        val userName: String? = ai.result.getStringParameter("user", null)
        val user: User? = if (event.channelType.isGuild && userName != null) {
            event.guild.getMembersByEffectiveName(userName, true).getOrNull(0)?.user
        } else {
            event.author
        }
        if (user == null) {
            event.message.reply("Unable to find the specified user!")
            return
        }
        event.message.reply(user.getinfoEmbed("User Info", "Moderation", null))
    }
}

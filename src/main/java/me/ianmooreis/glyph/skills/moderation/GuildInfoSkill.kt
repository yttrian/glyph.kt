package me.ianmooreis.glyph.skills.moderation

import ai.api.model.AIResponse
import me.ianmooreis.glyph.extensions.asPlainMention
import me.ianmooreis.glyph.extensions.getInfoEmbed
import me.ianmooreis.glyph.extensions.isBotFarm
import me.ianmooreis.glyph.extensions.reply
import me.ianmooreis.glyph.extensions.toDate
import me.ianmooreis.glyph.orchestrators.skills.Skill
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import org.ocpsoft.prettytime.PrettyTime

/**
 * A skill that allows users to ask for different info about a guild
 */
object GuildInfoSkill : Skill("skill.moderation.guildInfo", guildOnly = true) {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        val property: String? = ai.result.getStringParameter("guildProperty", null)
        if (property != null) {
            val guild = event.guild
            event.message.reply(when (property) {
                "name" -> "This guild is **${guild.name}**."
                "id" -> "The id for ${guild.name} is **${guild.id}**."
                "region" -> "${guild.name} is located in **${guild.regionRaw}**."
                "created" -> "${guild.name} was created **${PrettyTime().format(guild.creationTime.toDate())}** (${guild.creationTime})."
                "owner" -> "**${guild.owner.asPlainMention}** is the owner of ${guild.name}."
                "members" -> "${guild.name} has **${guild.members.count()}** members."
                "membersHumans" -> "${guild.name} has **${guild.members.count { !it.user.isBot }}** humans."
                "membersBots" -> "${guild.name} has **${guild.members.count { it.user.isBot }}** bots."
                "channels" -> "${guild.name} has **${guild.textChannels.size + guild.voiceChannels.size}** channels."
                "channelsText" -> "${guild.name} has **${guild.textChannels.size}** text channels."
                "channelsVoice" -> "${guild.name} has **${guild.voiceChannels.size}** voice channels."
                "roles" -> "${guild.name} has **${guild.roles.size}** roles."
                "farm" -> if (guild.isBotFarm) "**Yes**, ${guild.name} is a bot farm!" else "**No**, ${guild.name} is not a bot farm."
                else -> "I'm not sure what property `$property` is for a guild."
            })
        } else {
            event.message.reply(event.guild.getInfoEmbed("Guild Info", "Moderation", null, true))
        }
    }
}
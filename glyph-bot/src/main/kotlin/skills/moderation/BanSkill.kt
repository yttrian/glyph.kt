package org.yttr.glyph.bot.skills.moderation

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.yttr.glyph.bot.ai.AIResponse
import org.yttr.glyph.bot.directors.messaging.SimpleDescriptionBuilder
import org.yttr.glyph.bot.extensions.asPlainMention
import org.yttr.glyph.bot.extensions.sendDeathPM
import org.yttr.glyph.bot.messaging.Response
import org.yttr.glyph.bot.skills.Skill
import org.yttr.glyph.bot.skills.moderation.AuditingDirector.audit

/**
 * A skill that allows privileged members to ban other members
 */
class BanSkill : Skill(
    "skill.moderation.ban",
    guildOnly = true,
    requiredPermissionsSelf = listOf(Permission.BAN_MEMBERS),
    requiredPermissionsUser = listOf(Permission.BAN_MEMBERS)
) {
    override suspend fun onTrigger(event: MessageReceivedEvent, ai: AIResponse): Response =
        KickBanSkillHelper.getInstance(event, ai, "ban") { targets, reason ->
            event.message.delete().reason("Ban request").queue()
            targets.forEach { member ->
                member.user.sendDeathPM("***You have been banned from ${event.guild.name} for \"$reason\"!***") {
                    event.guild.ban(member, 7, reason).queue()
                }
            }
            val targetNames = targets.joinToString { it.asPlainMention }
            if (event.guild.config.auditing.bans) {
                val auditMessage = SimpleDescriptionBuilder()
                    .addField("Who", if (targetNames.length < 200) targetNames else "${targets.size} people")
                    .addField("Blame", event.author.asMention)
                    .addField("Reason", reason)
                    .build()
                event.guild.audit("Members Banned", auditMessage)
            }

            Response.Permanent("***${if (targetNames.length < 200) targetNames else "${targets.size} people"} ${if (targets.size == 1) "was" else "were"} banned!***")
        }
}

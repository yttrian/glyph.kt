package org.yttr.glyph.skills.moderation

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.yttr.glyph.ai.AIResponse
import org.yttr.glyph.directors.messaging.SimpleDescriptionBuilder
import org.yttr.glyph.extensions.asPlainMention
import org.yttr.glyph.extensions.sendDeathPM
import org.yttr.glyph.messaging.Response
import org.yttr.glyph.skills.Skill
import org.yttr.glyph.skills.moderation.AuditingDirector.audit

/**
 * A skill that allows privileged members to kick other members
 */
class KickSkill : Skill(
    "skill.moderation.kick",
    guildOnly = true,
    requiredPermissionsSelf = listOf(Permission.KICK_MEMBERS),
    requiredPermissionsUser = listOf(Permission.KICK_MEMBERS)
) {
    override suspend fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) =
        KickBanSkillHelper.getInstance(event, ai, "kick") { targets, reason ->
            event.message.delete().reason("Kick request").queue()
            targets.forEach { member ->
                member.user.sendDeathPM("***You have been kicked from ${event.guild.name} for \"$reason\"!***") {
                    event.guild.kick(member, reason).queue()
                }
            }
            val targetNames = targets.joinToString { it.asPlainMention }
            if (event.guild.config.auditing.kicks) {
                val auditMessage = SimpleDescriptionBuilder()
                    .addField("Who", if (targetNames.length < 200) targetNames else "${targets.size} people")
                    .addField("Blame", event.author.asMention)
                    .addField("Reason", reason)
                    .build()
                event.guild.audit("Members Kicked", auditMessage)
            }

            Response.Permanent("***${if (targetNames.length < 200) targetNames else "${targets.size} people"} ${if (targets.size == 1) "was" else "were"} kicked!***")
        }
}

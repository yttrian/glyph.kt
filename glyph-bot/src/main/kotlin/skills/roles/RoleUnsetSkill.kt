package org.yttr.glyph.bot.skills.roles

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.exceptions.HierarchyException
import org.yttr.glyph.bot.ai.AIResponse
import org.yttr.glyph.bot.extensions.asPlainMention
import org.yttr.glyph.bot.extensions.cleanMentionedMembers
import org.yttr.glyph.bot.messaging.Response
import org.yttr.glyph.bot.skills.Skill

/**
 * A skill to allow members to unset a selectable role
 */
class RoleUnsetSkill : Skill(
    "skill.role.unset",
    guildOnly = true,
    requiredPermissionsSelf = listOf(Permission.MANAGE_ROLES)
) {
    override suspend fun onTrigger(event: MessageReceivedEvent, ai: AIResponse): Response {
        //Check if the user is allowed to remove roles for the specified target(s)
        if ((event.message.cleanMentionedMembers.isNotEmpty() || event.message.mentionsEveryone()) && !event.member!!.hasPermission(
                listOf(Permission.MANAGE_ROLES)
            )
        ) {
            return Response.Volatile("You must have Manage Roles permission to remove other peoples' roles!")
        }

        return RoleSkillHelper.getInstance(event, ai, event.guild.config.selectableRoles) { desiredRole, _, targets ->
            //Remove the role
            targets.forEach {
                try {
                    event.guild.removeRoleFromMember(it, desiredRole)
                        .reason("Asked to not be $desiredRole.Name").queue()
                } catch (e: HierarchyException) {
                    this.log.debug("Can not remove role ${desiredRole.name} from members in ${event.guild}")
                }
            }
            val targetNames = targets.joinToString { it.asPlainMention }
            Response.Volatile("*${if (targetNames.length < 50) targetNames else "${targets.size} people"} ${if (targets.size == 1) "is" else "are"} no longer ${desiredRole.name}!*")
        }
    }
}

package org.yttr.glyph.skills.moderation

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.yttr.glyph.ai.AIResponse
import org.yttr.glyph.extensions.cleanMentionedMembers
import org.yttr.glyph.messaging.Response

/**
 * Helps run all the checks before allowing a member to kick or ban
 */
object KickBanSkillHelper {
    /**
     * Run checks and created a list of targets to kick or ban if allowed
     *
     * @param event the message event
     * @param ai the ai response
     * @param action the action the user is trying to perform (kick or ban)
     * @param success the call back to run if the targets are found and allowed
     */
    fun getInstance(
        event: MessageReceivedEvent,
        ai: AIResponse,
        action: String,
        success: (targets: List<Member>, reason: String) -> Response
    ): Response {
        val authorMaxRole = maxRolePosition(event.member!!)
        val selfMaxRole = maxRolePosition(event.guild.selfMember)
        val targets = event.message.cleanMentionedMembers
        return when {
            event.message.mentionsEveryone() -> Response.Volatile("You cannot $action everyone at once!")
            targets.isEmpty() -> Response.Volatile("You need to @mention at least one member to $action!")
            targets.contains(event.member!!) -> Response.Volatile("You cannot $action yourself!")
            targets.contains(event.guild.owner) -> Response.Volatile("You cannot $action the owner!")
            targets.filterNot { it.hasPermission(Permission.ADMINISTRATOR) }
                .isEmpty() -> Response.Volatile("I will not $action someone with Administrator permissions!")
            targets.filterNot { it.hasPermission(Permission.MANAGE_SERVER) }
                .isEmpty() -> Response.Volatile("I will not $action someone with Manage Server permissions!")
            targets.contains(event.guild.selfMember) -> Response.Volatile(
                "I cannot kick myself!"
            )
            event.message.cleanMentionedMembers.filterNot { maxRolePosition(it) >= authorMaxRole }
                .isEmpty() && !event.member!!.isOwner -> Response.Volatile(
                "You cannot $action members of your role or higher!"
            )
            event.message.cleanMentionedMembers.filterNot { maxRolePosition(it) >= selfMaxRole }
                .isEmpty() -> Response.Volatile(
                "I cannot $action members of my role or higher!"
            )
            else -> success(
                targets,
                ai.result.getStringParameter("reason") ?: "No reason provided"
            )
        }
    }

    private fun maxRolePosition(member: Member): Int {
        return member.roles.map { it.position }.maxOrNull() ?: 0
    }
}
